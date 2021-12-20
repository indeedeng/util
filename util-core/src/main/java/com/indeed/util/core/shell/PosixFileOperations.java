package com.indeed.util.core.shell;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;

/**
 * @author jplaisance
 */
public final class PosixFileOperations {

    private static final Logger log = LoggerFactory.getLogger(PosixFileOperations.class);

    private static final String separator = System.getProperty("file.separator");

    public static void atomicLink(File target, File link) throws IOException {
        File tmpLink = File.createTempFile("tmp", ".link", link.getParentFile());
        try {
            link(target, tmpLink);
        } catch (IOException e) {
            tmpLink.delete();
            throw e;
        }
        tmpLink.renameTo(link);
    }

    public static void link(File target, File link) throws IOException {
        File linkParent = link.getParentFile();
        String relPath = relativePath(linkParent, target);
        Process lnProc = Runtime.getRuntime().exec(
                new String[]{"ln", "-sfn", relPath, link.getName()},
                null,
                linkParent
        );
        try {
            int result = lnProc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                lnProc,
                                "error running ln process, exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    public static void rmrf(File file) throws IOException {
        Process rmProc = Runtime.getRuntime().exec(new String[]{"rm", "-rf", file.getAbsolutePath()}, null);
        try {
            int result = rmProc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                rmProc,
                                "error running rm process, exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    public static void rmrf(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                super.postVisitDirectory(dir, exc);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void cplr(File src, File dest) throws IOException {
        Process cpProc = Runtime.getRuntime().exec(new String[]{"cp", "-lr", src.getAbsolutePath(), dest.getAbsolutePath()}, null);
        try {
            int result = cpProc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                cpProc,
                                "error running cp process, exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    private static void fsyncDir(final Path dir) throws IOException {
        try (FileChannel channel = FileChannel.open(dir, StandardOpenOption.READ)) {
            channel.force(true);
        }
    }

    public static void cplr(final Path src, final Path dest) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                super.preVisitDirectory(dir, attrs);
                Files.createDirectory(dest.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs);
                Files.createLink(dest.resolve(src.relativize(file)), file);
                return FileVisitResult.CONTINUE;
            }
        });

        fsyncDir(dest);
    }

    public static void recursiveCopy(File srcDir, File destDir) throws IOException {
        Process cpProc = Runtime.getRuntime().exec(new String[]{"cp", "-Lr", srcDir.getAbsolutePath(), destDir.getAbsolutePath()}, null);
        try {
            int result = cpProc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                cpProc,
                                "error running cp process, exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    public static void recursiveCopy(final Path src, final Path dest) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                super.preVisitDirectory(dir, attrs);
                Files.createDirectory(dest.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs);
                Files.copy(file, dest.resolve(src.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });

        fsyncDir(dest);
    }

    public static long du(File path) throws IOException {
        Process proc = Runtime.getRuntime().exec(new String[]{"du", "-bs", path.getPath()});
        try {
            int result = proc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                proc,
                                "error running du -bs "+path.getPath()+", exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
            StringWriter out = new StringWriter();
            CharStreams.copy(new InputStreamReader(proc.getInputStream(), Charsets.UTF_8), out);
            final String str = out.toString().trim();
            final int index = str.indexOf('\t');
            return Long.parseLong(str.substring(0, index));
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    public static String relativePath(String base, String path) {
        return relativePath(new File(base), new File(path));
    }

    public static String relativePath(File base, File path) {
        ArrayDeque<String> baseParts = getParts(base);
        ArrayDeque<String> pathParts = getParts(path);
        StringBuilder ret = new StringBuilder();
        while (!baseParts.isEmpty() && !pathParts.isEmpty() && baseParts.getLast().equals(pathParts.getLast())) {
            baseParts.removeLast();
            pathParts.removeLast();
        }
        while (!baseParts.isEmpty()) {
            ret.append("..");
            ret.append(separator);
            baseParts.removeLast();
        }
        while (!pathParts.isEmpty()) {
            String part = pathParts.removeLast();
            ret.append(part);
            if (!pathParts.isEmpty()) ret.append(separator);
        }
        return ret.toString();
    }
    
    public static String lsla(File file) throws IOException {
        Process proc = Runtime.getRuntime().exec(new String[]{"ls", "-la"}, null, file);
        try {
            final int result = proc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                proc,
                                "error running ls -la process, exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        )
                );
            }
            final StringWriter out = new StringWriter();
            CharStreams.copy(new InputStreamReader(proc.getInputStream(), Charsets.UTF_8), out);
            return out.toString();
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    private static ArrayDeque<String> getParts(File file) {
        ArrayDeque<String> ret = new ArrayDeque<String>();
        File fileParent = file;
        while (fileParent != null) {
            ret.add(fileParent.getName());
            fileParent = fileParent.getParentFile();
        }
        return ret;
    }

    private static String formatWithStdStreams(Process proc, String fmt) throws IOException {
        return String.format(
                fmt,
                new String(ByteStreams.toByteArray(proc.getInputStream()), Charsets.UTF_8),
                new String(ByteStreams.toByteArray(proc.getErrorStream()), Charsets.UTF_8)
        );
    }
    
    public static int getPID() throws IOException {
        Process proc = Runtime.getRuntime().exec(new String[]{"bash", "-c", "echo $PPID"});
        try {
            int result = proc.waitFor();
            if (result != 0) {
                throw new IOException(
                        formatWithStdStreams(
                                proc,
                                "error running bash -c \"echo $PPID\", exit code: " + result + "\nstdout:\n%s\nstderr:\n%s"
                        ));
            }
            StringWriter out = new StringWriter();
            CharStreams.copy(new InputStreamReader(proc.getInputStream(), Charsets.UTF_8), out);
            return Integer.parseInt(out.toString().trim());
        } catch (InterruptedException e) {
            log.error("exception during exec", e);
            throw new IOException("exec failed", e);
        }
    }

    public static boolean isProcessRunning(int pid, boolean assumeRunning) {
        File procfs = new File("/proc");
        if (assumeRunning && !procfs.exists()) {
            return true;
        }
        return new File(procfs, String.valueOf(pid)).exists();
    }
    
    public static Integer tryParseInt(String str) {
        return tryParseInt(str, 10);
    }
    
    public static Integer tryParseInt(String str, int radix) {
        if (str.length() == 0) return null;
        int value = 0;
        for (int i = 0; i < str.length(); i++) {
            final int digit = Character.digit(str.charAt(i), radix);
            if (digit < 0) return null;
            value = value*radix+digit;
        }
        return value;
    }
}
