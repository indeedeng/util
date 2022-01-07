// $Id$
package com.indeed.util.io;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Files {
    // you may wish to use this particular logger sparingly, as many times it can be more helpful
    // if you log stuff to a more specific (context-specific) logger than something in common-util
    private static final Logger LOGGER = LoggerFactory.getLogger(Files.class);

    private Files() {}

    public static String buildPath(String... parts) {
        if (parts.length == 0) return null;
        if (parts.length == 1) return parts[0];
        File temp = new File(parts[0], parts[1]);
        for (int i = 2; i < parts.length; i++) {
            temp = new File(temp, parts[i]);
        }
        return temp.getPath();
    }

    /**
     * Serializes an object to a file, throws an exception if it fails
     *
     * @param obj object to write to a file
     * @param file path to save the object to
     * @throws java.io.IOException if the existing file could not be erased, or the file could not
     *     be written, flushed, synced, or closed
     */
    public static void writeObjectToFileOrDie2(
            @Nonnull final Object obj, @Nonnull final String file) throws IOException {
        Preconditions.checkNotNull(file, "file argument is required!");
        Preconditions.checkArgument(!file.isEmpty(), "file argument is required!");

        // todo: should 'obj' be required? do we ever WANT to write 'null' to an artifact?
        Preconditions.checkNotNull(obj, "cannot write a 'null' object");

        final File targetFile = new File(file);

        // write object to temporary file that is flushed, fsynced, and closed by the time it
        // returns
        final ObjectOutputStreamCallback callback = new ObjectOutputStreamCallback(obj);
        final File tmpFile = writeDataToTempFileOrDie2(callback, targetFile);
        final long checksumForWrittenData = callback.getChecksumValue();

        // verify that what we WROTE to the disk is then immediately READABLE before allowing the
        // rename to happen
        final long checksumFound = computeFileChecksum(tmpFile, new CRC32());
        if (checksumForWrittenData != checksumFound) {
            throw new IOException(
                    "Data written to file is not what we expected, "
                            + checksumFound
                            + " != "
                            + checksumForWrittenData
                            + ": "
                            + tmpFile);
        }

        if (!tmpFile.renameTo(targetFile)) {
            // failed to atomically rename from temp file to target file, so throw an exception,
            // leaving the filesystem in
            // a sane state at all times
            throw new IOException("Could not rename '" + tmpFile + "' to '" + targetFile + "'.");
        }
    }

    /** @deprecated Use {@link #writeObjectToFileOrDie2(java.lang.Object, java.lang.String)} */
    @Deprecated
    public static void writeObjectToFileOrDie(
            @Nonnull final Object obj,
            @Nonnull final String file,
            @Nonnull final org.apache.log4j.Logger log)
            throws IOException {
        writeObjectToFileOrDie2(obj, file);
    }

    private static class ObjectOutputStreamCallback implements OutputStreamCallback {
        private long checksumForWrittenData = 0L;
        @Nonnull private final Object obj;

        private ObjectOutputStreamCallback(@Nonnull Object obj) {
            this.obj = obj;
        }

        public long getChecksumValue() {
            return checksumForWrittenData;
        }

        @Override
        public void writeAndFlushData(@Nonnull OutputStream outputStream) throws IOException {
            final ChecksummingOutputStream checksummingOutputStream =
                    new ChecksummingOutputStream(new BufferedOutputStream(outputStream));
            final ObjectOutputStream out = new ObjectOutputStream(checksummingOutputStream);

            // write the data
            out.writeObject(obj);
            // flush the various streams
            out.flush();

            checksumForWrittenData = checksummingOutputStream.getChecksumValue();
        }
    }

    private static class ChecksummingOutputStream extends FilterOutputStream {
        @Nonnull private final Checksum checksummer;

        private ChecksummingOutputStream(OutputStream out) {
            super(out);
            checksummer = new CRC32();
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            checksummer.update(b & 0xff);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            checksummer.update(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            checksummer.update(b, off, len);
        }

        /**
         * Returns the current checksum value.
         *
         * @return the current checksum value
         */
        public long getChecksumValue() {
            return checksummer.getValue();
        }
    }

    private static interface OutputStreamCallback {
        void writeAndFlushData(@Nonnull final OutputStream outputStream) throws IOException;
    }

    // return a reference to a temp file that contains the written + flushed + fsynced + closed data
    @Nonnull
    private static File writeDataToTempFileOrDie2(
            @Nonnull final OutputStreamCallback callback, @Nonnull final File targetFile)
            throws IOException {
        Preconditions.checkNotNull(callback, "callback argument is required!");
        Preconditions.checkNotNull(targetFile, "targetFile argument is required!");

        FileOutputStream fileOut = null;
        FileChannel fileChannel = null;
        try {
            final String targetFinalName = targetFile.getName();
            final File targetDirectory = targetFile.getParentFile();

            // open temporary file
            final File tmpFile = File.createTempFile(targetFinalName, ".tmp", targetDirectory);

            fileOut = new FileOutputStream(tmpFile);
            fileChannel = fileOut.getChannel();

            // make sure to use an output stream that flows THROUGH the FileChannel, so that
            // FileChannel.force(true)
            // can do what it's supposed to

            // write the data AND flush it
            callback.writeAndFlushData(Channels.newOutputStream(fileChannel));

            return tmpFile;
        } finally {
            try {
                // fsync to disk (both data AND length)
                if (fileChannel != null) {
                    fileChannel.force(true);
                }
            } finally {
                // close the open file (if during an EXC,
                if (fileOut != null) {
                    fileOut.close();
                }
            }
        }
    }

    /**
     * @deprecated Use {@link
     *     Files#writeDataToTempFileOrDie2(com.indeed.util.io.Files.OutputStreamCallback,
     *     java.io.File)}
     */
    @Deprecated
    @Nonnull
    private static File writeDataToTempFileOrDie(
            @Nonnull final OutputStreamCallback callback,
            @Nonnull final File targetFile,
            @Nonnull final org.apache.log4j.Logger log)
            throws IOException {
        return writeDataToTempFileOrDie2(callback, targetFile);
    }

    @Nonnull
    private static File writeTextToTempFileOrDie2(
            @Nonnull final String[] text, @Nonnull final File targetFile) throws IOException {
        Preconditions.checkNotNull(text, "callback argument is required!");
        Preconditions.checkNotNull(targetFile, "targetFile argument is required!");

        final String targetFinalName = targetFile.getName();
        final File targetDirectory = targetFile.getParentFile();

        // open temporary file
        final File tmpFile = File.createTempFile(targetFinalName, ".tmp", targetDirectory);

        FileOutputStream fileOut = new FileOutputStream(tmpFile);
        Writer writer = new PrintWriter(new OutputStreamWriter(fileOut, Charsets.UTF_8));
        FileChannel fileChannel = null;
        try {
            fileChannel = fileOut.getChannel();
            for (String line : text) {
                writer.write(line);
                writer.write('\n');
            }
            writer.flush();
            return tmpFile;
        } finally {
            try {
                // fsync to disk (both data AND length)
                if (fileChannel != null) {
                    fileChannel.force(true);
                }
            } finally {
                fileOut.close();
            }
        }
    }

    /** @deprecated Use {@link #writeTextToTempFileOrDie2(java.lang.String[], java.io.File)} */
    @Deprecated
    @Nonnull
    private static File writeTextToTempFileOrDie(
            @Nonnull final String[] text,
            @Nonnull final File targetFile,
            @Nonnull final org.apache.log4j.Logger log)
            throws IOException {
        return writeTextToTempFileOrDie2(text, targetFile);
    }

    /**
     * Writes an object to a file only if it is different from the current contents of the file, or
     * if the file does not exist. Note that you must have enough heap to contain the entire
     * contents of the object graph.
     *
     * @param obj object to write to a file
     * @param file path to save the object to
     * @return true if the file was actually written, false if the file was unchanged
     * @throws java.io.IOException if the existing file could not be read for comparison, if the
     *     existing file could not be erased, or if the new file could not be written, flushed,
     *     synced, or closed
     */
    public static boolean writeObjectIfChangedOrDie2(
            @Nonnull final Object obj, @Nonnull final String file) throws IOException {
        Preconditions.checkNotNull(file, "file argument is required!");
        Preconditions.checkArgument(!file.isEmpty(), "file argument is required!");

        // todo: should 'obj' be required? do we ever WANT to write 'null' to an artifact?
        Preconditions.checkNotNull(obj, "cannot write a 'null' object to file %s", file);

        // first serialize the object into a byte array, this should almost never fail
        final IndexableByteArrayOutputStream baos = new IndexableByteArrayOutputStream(524288);
        {
            final ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.close();
            baos.close();
        }

        if (isChanged(baos.unsafeByteArrayView(), baos.size(), file)) {
            // compute the checksum of what we intend on writing to disk
            final Checksum checksum = new CRC32();
            checksum.update(baos.unsafeByteArrayView(), 0, baos.size());
            final long checksumForWrittenData = checksum.getValue();

            final File targetFile = new File(file);

            // write object to temporary file that is flushed, fsynced, and closed by the time it
            // returns
            final File tmpFile =
                    writeDataToTempFileOrDie2(
                            outputStream -> {
                                // write the data
                                baos.writeTo(outputStream);
                                // flush it, so that the fsync() has everything it needs
                                outputStream.flush();
                            },
                            targetFile);

            // verify that what we WROTE to the disk is then immediately READABLE before allowing
            // the rename to happen
            checksum.reset();
            final long checksumFound = computeFileChecksum(tmpFile, new CRC32());
            if (checksumForWrittenData != checksumFound) {
                throw new IOException(
                        "Data written to file is not what we expected, "
                                + checksumFound
                                + " != "
                                + checksumForWrittenData
                                + ": "
                                + tmpFile);
            }

            if (!tmpFile.renameTo(targetFile)) {
                // failed to atomically rename from temp file to target file, so throw an exception,
                // leaving the filesystem in
                // a sane state at all times
                throw new IOException(
                        "Could not rename '" + tmpFile + "' to '" + targetFile + "'.");
            }

            return true;
        } else {
            return false;
        }
    }

    /** @deprecated Use {@link #writeObjectIfChangedOrDie2(java.lang.Object, java.lang.String)} */
    @Deprecated
    public static boolean writeObjectIfChangedOrDie(
            @Nonnull final Object obj,
            @Nonnull final String file,
            @Nonnull final org.apache.log4j.Logger log)
            throws IOException {
        return writeObjectIfChangedOrDie2(obj, file);
    }

    public static long computeFileChecksum(
            @Nonnull final File file, @Nonnull final Checksum checksum) throws IOException {
        return com.google.common.io.Files.asByteSource(file).hash(Hashing.crc32()).padToLong();
    }

    /**
     * Writes an object to a file.
     *
     * @return true if the file was successfully written, false otherwise
     * @deprecated use {@link #writeObjectToFileOrDie2(Object, String)} instead
     */
    @Deprecated
    public static boolean writeObjectToFile(Object obj, String file) {
        try {
            writeObjectToFileOrDie2(obj, file);
            return true;
        } catch (Exception e) {
            LOGGER.error(
                    e.getClass()
                            + ": writeObjectToFile("
                            + file
                            + ") encountered exception: "
                            + e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * Writes an object to a file only if it is different from the current contents of the file, or
     * if the file does not exist. Note that you must have enough heap to contain the entire
     * contents of the object graph.
     *
     * @return true if the file was actually written, false otherwise
     * @deprecated use {@link #writeObjectIfChangedOrDie2(Object, String)} instead
     */
    @Deprecated
    public static boolean writeObjectIfChanged(Object obj, String filepath) {
        try {
            return writeObjectIfChangedOrDie2(obj, filepath);
        } catch (Exception e) {
            LOGGER.error(
                    e.getClass()
                            + ": writeObjectIfChanged("
                            + filepath
                            + ") encountered exception: "
                            + e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * Returns true iff the bytes in an array are different from the bytes contained in the given
     * file, or if the file does not exist.
     */
    private static boolean isChanged(final byte[] bytes, final int length, final String filepath)
            throws IOException {
        Preconditions.checkArgument(length >= 0, "invalid length value: %s", length);
        Preconditions.checkArgument(bytes.length >= length, "invalid length value: %s", length);

        File file = new File(filepath);
        if (!file.exists()) {
            return true;
        }
        if (file.length() != length) {
            return true;
        }
        final int BUFLEN = 1048576; // 1 megabyte
        byte[] buffer = new byte[BUFLEN];
        InputStream is = new FileInputStream(file);
        try {
            int len;
            for (int offset = 0; ; offset += len) {
                len = is.read(buffer);
                if (len < 0) break; // eof
                if (!arrayCompare(bytes, offset, buffer, 0, len)) return true;
            }
            return false;
        } finally {
            is.close();
        }
    }

    /** Returns true if the array chunks are equal, false otherwise. */
    private static boolean arrayCompare(byte[] a, int offset1, byte[] a2, int offset2, int length) {
        for (int i = 0; i < length; i++) {
            if (a[offset1++] != a2[offset2++]) return false;
        }
        return true;
    }

    /**
     * Reads an object of type {@code T} from {@code file}.
     *
     * @param file file from which the object should be read
     * @param clazz non-null Class object for {@code T}
     * @param printException whether or not any stacktraces should be printed
     * @param <T> the return type
     * @return possibly null object of type {@code T}.
     */
    public static <T> T readObjectFromFile(String file, Class<T> clazz, boolean printException) {
        final FileInputStream fileIn;
        try {
            fileIn = new FileInputStream(file);
        } catch (Exception e) {
            printException(e, printException);
            return null;
        }

        final BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
        final ObjectInputStream objIn;
        try {
            objIn = new ObjectInputStream(bufferedIn);
        } catch (Exception e) {
            printException(e, printException);
            closeInputStream(fileIn, printException);
            return null;
        }

        final Object ret;
        try {
            ret = objIn.readObject();
        } catch (Exception e) {
            printException(e, printException);
            closeInputStream(objIn, printException); // objIn.close() also closes fileIn
            return null;
        }

        closeInputStream(objIn, printException); // objIn.close() also closes fileIn
        return clazz.cast(ret);
    }

    /**
     * Convenience for {@link #readObjectFromFile(String file, Class clazz, boolean printException)}
     * where:
     *
     * <ul>
     *   <li>{@code clazz} is Object.class
     *   <li>{@code printException} is false
     * </ul>
     */
    public static Object readObjectFromClasspathDir(String file) {
        return readObjectFromClasspathDir(file, Object.class, false);
    }

    public static <T> T readObjectFromClasspathDir(
            String file, Class<T> clazz, boolean printException) {
        // final FileInputStream fileIn;
        /*try {
            fileIn = new FileInputStream(file);
        } catch (Exception e) {
            printException(e, printException);
            return null;
        } */

        final InputStream inStream = Files.class.getResourceAsStream(file);
        final BufferedInputStream bufferedIn = new BufferedInputStream(inStream);
        final ObjectInputStream objIn;
        try {
            objIn = new ObjectInputStream(bufferedIn);
        } catch (Exception e) {
            printException(e, printException);
            closeInputStream(inStream, printException);
            return null;
        }

        final Object ret;
        try {
            ret = objIn.readObject();
        } catch (Exception e) {
            printException(e, printException);
            closeInputStream(objIn, printException); // objIn.close() also closes fileIn
            return null;
        }

        closeInputStream(objIn, printException); // objIn.close() also closes fileIn
        return clazz.cast(ret);
    }

    private static void closeInputStream(final InputStream in, final boolean printException) {
        try {
            in.close();
        } catch (Exception e) {
            printException(e, printException);
        }
    }

    private static void printException(final Exception e, final boolean reallyPrintIt) {
        if (!reallyPrintIt) return;
        e.printStackTrace();
    }

    /**
     * Convenience for {@link #readObjectFromFile(String file, Class clazz, boolean printException)}
     * where:
     *
     * <ul>
     *   <li>{@code printException} is false
     * </ul>
     */
    public static <T> T readObjectFromFile(String file, Class<T> clazz) {
        return readObjectFromFile(file, clazz, false);
    }

    /**
     * Convenience for {@link #readObjectFromFile(String file, Class clazz, boolean printException)}
     * where:
     *
     * <ul>
     *   <li>{@code clazz} is Object.class
     * </ul>
     */
    public static Object readObjectFromFile(String file, boolean printException) {
        return readObjectFromFile(file, Object.class, printException);
    }

    /**
     * Convenience for {@link #readObjectFromFile(String file, Class clazz, boolean printException)}
     * where:
     *
     * <ul>
     *   <li>{@code clazz} is Object.class
     *   <li>{@code printException} is false
     * </ul>
     */
    public static Object readObjectFromFile(String file) {
        return readObjectFromFile(file, Object.class, false);
    }

    public static String[] readTextFile(String file) {
        try {
            return readTextFileOrDie(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] readTextFileOrDie(String file) throws IOException {
        ArrayList<String> contents = new ArrayList<String>();

        BufferedReader reader = getBufferedReaderForUtf8(file);
        try {
            String line = reader.readLine();
            while (line != null) {
                contents.add(line);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return contents.toArray(new String[contents.size()]);
    }

    /**
     * Reads all the lines in the given file, truncating everything that happens after # (including
     * the #)
     *
     * @param file
     * @return a List of the lines in the file in the order they appear (whitespace trimmed)
     * @throws IOException
     */
    public static List<String> readCommentedTextFile(final String file) throws IOException {
        BufferedReader reader = getBufferedReaderForUtf8(file);
        try {
            final List<String> lines = new ArrayList<String>();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String trimmed = line.trim();
                if (trimmed.length() == 0) {
                    continue;
                }
                final int hashIndex = trimmed.indexOf('#');
                if (hashIndex == -1) {
                    lines.add(trimmed);
                } else {
                    final String realPart = trimmed.substring(0, hashIndex).trim();
                    if (realPart.length() > 0) {
                        lines.add(realPart);
                    }
                }
            }
            return lines;
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                // intentionally empty
            }
        }
    }

    public static int[] readIntsFromTextFile(String file) {
        String[] strings = readTextFile(file);
        if (strings == null) return null;
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++) ints[i] = Integer.parseInt(strings[i]);
        return ints;
    }

    public static float[] readFloatsFromTextFile(String file) {
        String[] strings = readTextFile(file);
        if (strings == null) return null;
        float[] floats = new float[strings.length];
        for (int i = 0; i < strings.length; i++) floats[i] = Float.parseFloat(strings[i]);
        return floats;
    }

    @Deprecated
    public static void writeToTextFile(String[] lines, String file) {
        try {
            final BufferedWriter bufferedWriter = getBufferedWriterForUtf8(file);
            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToTextFileOrDie(
            @Nonnull final String[] lines, @Nonnull final String file) throws IOException {
        // Write out a temp file (or die)
        final File f = new File(file);
        final File temp = writeTextToTempFileOrDie2(lines, f);
        // Rename the temp file if writing succeeded
        if (!temp.renameTo(f)) {
            throw new IOException(
                    String.format("couldn't rename %s to %s", temp.getCanonicalPath(), file));
        }
    }

    public static void appendToTextFile(String[] lines, String file) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(file, true));
            for (String line : lines) {
                out.println(line);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTempDirectory(String prefix, String suffix) throws IOException {
        return getTempDirectory(prefix, suffix, (File) null);
    }

    public static String getTempDirectory(String prefix, String suffix, String directory)
            throws IOException {
        File dir = null;
        if (directory != null) {
            dir = new File(directory);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException(
                            "directory did not exist and could not be created: " + directory);
                }
            }
        }
        return getTempDirectory(prefix, suffix, dir);
    }

    public static String getTempDirectory(String prefix, String suffix, File directory)
            throws IOException {
        File f = File.createTempFile(prefix, suffix, directory);
        f.delete();
        f.mkdir();
        return f.getAbsolutePath();
    }

    /**
     * Deletes file or recursively deletes a directory
     *
     * <p>NOTE: this returns true if the file was actually deleted, and false for 2 cases: 1. file
     * did not exist to start with 2. File.delete() failed at some point for some reason
     *
     * <p>use {@link #deleteOrDie(String)} instead if you want a clearer distinction between the
     * 'falsy' responses
     *
     * @param file path to erase
     * @return true if all deletions were successful. If a deletion fails, the method stops
     *     attempting to delete and returns false
     */
    public static boolean delete(String file) {
        File f = new File(file);

        if (f.isDirectory()) {
            // first insure the directory is empty
            String[] children = f.list();
            for (String child : children) {
                if (!delete(Files.buildPath(file, child))) return false;
            }
        }

        return f.delete();
    }

    /**
     * Deletes file or recursively deletes a directory
     *
     * @param file path to erase
     * @return true if all deletions were successful, false if file did not exist
     * @throws IOException if deletion fails and the file still exists at the end
     */
    public static boolean deleteOrDie(@Nonnull final String file) throws IOException {
        // this returns true if the file was actually deleted
        // and false for 2 cases:
        //   1. file did not exist to start with
        //   2. File.delete() failed at some point for some reason
        // so we disambiguate the 'false' case below by checking for file existence
        final boolean fileWasDeleted = delete(file);

        if (fileWasDeleted) {
            // file was definitely deleted
            return true;
        } else {
            final File fileObj = new File(file);
            if (fileObj.exists()) {
                throw new IOException(
                        "File still exists after erasure, cannot write object to file: " + file);
            }

            // file was not deleted, because it does not exist
            return false;
        }
    }

    /**
     * Use this function instead of {@link java.io.File#getCanonicalPath()}, as that method can fail
     * during symlink changes, and this method will retry up to 3 times with a short delay. Returns
     * null if unsuccessful after retrying.
     *
     * @param path path to canonicalize
     * @return the canonical pathname, or null if unsuccessful
     */
    public static String getCanonicalPath(String path) {
        if (path == null) return null;
        return getCanonicalPath(
                path,
                new Supplier<Boolean>() {
                    int retries = 2;

                    public Boolean get() {
                        if (retries-- == 0) {
                            return false;
                        }
                        try {
                            // if retries not exhausted, sleep 100 milliseconds
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            return false;
                        }
                        return true;
                    }
                });
    }

    static String getCanonicalPath(String path, Supplier<Boolean> retryPolicy) {
        boolean shouldRetry = true;
        while (shouldRetry) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return file.getCanonicalPath();
                } catch (IOException e) {
                    // try again I guess
                }
            }

            shouldRetry = retryPolicy.get();
        }
        return null;
    }

    /**
     * Gets the directory name of the canonical path -- the last element in the result of {@link
     * #getCanonicalPath(String)}.
     *
     * @param path path to canonicalize
     * @return canonical directory name
     */
    public static String getCanonicalDirectoryName(String path) {
        String realPath = getCanonicalPath(path);
        if (realPath == null) {
            return null;
        }

        String separator = System.getProperty("file.separator", "/");
        // backslashes must be escaped for split().  kthxbai
        separator = Pattern.quote(separator);
        String[] pathElements = realPath.split(separator);
        return pathElements.length > 0 ? pathElements[pathElements.length - 1] : realPath;
    }

    public static byte[] loadFileAsByteArray(final String file) throws IOException {
        final InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream(10000);
            while (true) {
                final int val = inputStream.read();
                if (val == -1) break;
                out.write(val);
            }
            return out.toByteArray();
        } finally {
            inputStream.close();
        }
    }

    public static String getFileHash(final String file, final String algorithm)
            throws IOException, NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(algorithm);
        return Files.toHex(md.digest(loadFileAsByteArray(file)));
    }

    /**
     * Converts a byte array to a hex string. The String returned will be of length exactly {@code
     * bytes.length * 2}.
     */
    @Nonnull
    public static String toHex(@Nonnull final byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hexDigits = Integer.toHexString((int) b & 0x00ff);
            if (hexDigits.length() == 1) {
                buf.append('0');
            }
            buf.append(hexDigits);
        }
        return buf.toString();
    }

    public static BufferedReader getBufferedReaderForUtf8(String file)
            throws FileNotFoundException {
        try {
            final FileInputStream fileInputStream = new FileInputStream(file);
            final InputStreamReader inputStreamReader =
                    new InputStreamReader(fileInputStream, "UTF-8");
            return new BufferedReader(inputStreamReader);
        } catch (UnsupportedEncodingException uee) {
            // Should never occur!
            throw new RuntimeException(uee);
        }
    }

    private static BufferedWriter getBufferedWriterForUtf8(String file)
            throws FileNotFoundException {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(fileOutputStream, "UTF-8");
            return new BufferedWriter(outputStreamWriter);
        } catch (UnsupportedEncodingException uee) {
            // Should never occur!
            throw new RuntimeException(uee);
        }
    }

    public static String readStreamAsString(final InputStream in) throws IOException {
        return readStreamAsString(in, 256);
    }

    public static String readStreamAsString(final InputStream in, final int bufferSize)
            throws IOException {
        final StringBuilder sb = new StringBuilder();
        final char[] buffer = new char[bufferSize];
        final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in));
        for (int read = reader.read(buffer); read != -1; read = reader.read(buffer)) {
            if (read > 0) {
                sb.append(buffer, 0, read);
            }
        }

        return sb.toString();
    }

    /**
     * Use this instead of {@link FileWriter} because you cannot specify the character encoding with
     * that.
     */
    public static Writer newBufferedUTF8FileWriter(final String file)
            throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
    }

    /**
     * Use this instead of {@link FileWriter} because you cannot specify the character encoding with
     * that.
     */
    public static Writer newBufferedUTF8FileWriter(final File file)
            throws UnsupportedEncodingException, FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
    }
}
