package com.indeed.util.core.shell;

import com.google.common.base.Charsets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author kenh
 */

public class PosixFileOperationsTest {
    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private class CountFileVisitor extends SimpleFileVisitor<Path> {
        int dirs = 0;
        int files = 0;

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            ++dirs;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            ++files;
            return FileVisitResult.CONTINUE;
        }
    }

    private class ConcatFileVisitor extends SimpleFileVisitor<Path> {
        final List<String> conents = new ArrayList<>();

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            conents.add(new String(Files.readAllBytes(file), Charsets.UTF_8));
            return FileVisitResult.CONTINUE;
        }
    }

    @Test
    public void testRmRf() throws IOException {
        final Path rootDir = tempDir.getRoot().toPath();
        final Path aDir = Files.createDirectory(rootDir.resolve("a"));
        final Path file1 = Files.createFile(aDir.resolve("file1"));
        final Path file2 = Files.createFile(aDir.resolve("file2"));

        final Path bDir = Files.createDirectory(aDir.resolve("b"));
        final Path file3 = Files.createFile(bDir.resolve("file3"));
        final Path file4 = Files.createFile(bDir.resolve("file4"));

        final Path cDir = Files.createDirectory(aDir.resolve("c"));
        final Path file5 = Files.createFile(cDir.resolve("file5"));
        final Path file6 = Files.createFile(cDir.resolve("file6"));

        final Path dDir = Files.createDirectory(cDir.resolve("d"));
        final Path file7 = Files.createFile(dDir.resolve("file7"));
        final Path file8 = Files.createFile(dDir.resolve("file8"));

        {
            final CountFileVisitor countFileVisitor = new CountFileVisitor();
            Files.walkFileTree(rootDir, countFileVisitor);

            Assert.assertEquals(5, countFileVisitor.dirs); // root, a, b, c, d
            Assert.assertEquals(8, countFileVisitor.files); // file1 - file8
        }

        PosixFileOperations.rmrf(cDir.toFile());

        {
            final CountFileVisitor countFileVisitor = new CountFileVisitor();
            Files.walkFileTree(rootDir, countFileVisitor);

            Assert.assertEquals(3, countFileVisitor.dirs); // root, a, b
            Assert.assertEquals(4, countFileVisitor.files); // file1 - file4
        }

        PosixFileOperations.rmrf(aDir);

        {
            final CountFileVisitor countFileVisitor = new CountFileVisitor();
            Files.walkFileTree(rootDir, countFileVisitor);

            Assert.assertEquals(1, countFileVisitor.dirs); // root
            Assert.assertEquals(0, countFileVisitor.files);
        }

    }

    private void writeToFile(final Path path, final String contents) throws IOException {
        try (final OutputStream os = Files.newOutputStream(path)) {
            os.write(contents.getBytes());
        }
    }

    @Test
    public void testCpLr() throws IOException {
        final Path rootDir = tempDir.getRoot().toPath();
        final Path aDir = Files.createDirectory(rootDir.resolve("a"));
        writeToFile(aDir.resolve("file1"), "file1");
        writeToFile(aDir.resolve("file2"), "file2");

        final Path bDir = Files.createDirectory(aDir.resolve("b"));
        writeToFile(bDir.resolve("file3"), "file3");
        writeToFile(bDir.resolve("file4"), "file4");

        final Path cDir = Files.createDirectory(aDir.resolve("c"));
        writeToFile(cDir.resolve("file5"), "file5");
        writeToFile(cDir.resolve("file6"), "file6");

        final Path dDir = Files.createDirectory(cDir.resolve("d"));
        writeToFile(dDir.resolve("file7"), "file7");
        writeToFile(dDir.resolve("file8"), "file8");

        {
            final CountFileVisitor countFileVisitor = new CountFileVisitor();
            Files.walkFileTree(rootDir, countFileVisitor);

            Assert.assertEquals(5, countFileVisitor.dirs); // root, a, b, c, d
            Assert.assertEquals(8, countFileVisitor.files); // file1 - file8
        }

        {
            final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
            Files.walkFileTree(rootDir, concatFileVisitor);
            Collections.sort(concatFileVisitor.conents);

            Assert.assertEquals(
                    Arrays.asList(
                            "file1",
                            "file2",
                            "file3",
                            "file4",
                            "file5",
                            "file6",
                            "file7",
                            "file8"
                    ),
                    concatFileVisitor.conents);
        }

        {
            final Path aDirCopy = rootDir.resolve("acopy");
            PosixFileOperations.cplr(aDir, aDirCopy);

            {
                final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
                Files.walkFileTree(aDirCopy, concatFileVisitor);
                Collections.sort(concatFileVisitor.conents);

                Assert.assertEquals(
                        Arrays.asList(
                                "file1",
                                "file2",
                                "file3",
                                "file4",
                                "file5",
                                "file6",
                                "file7",
                                "file8"
                        ),
                        concatFileVisitor.conents);
            }

            // adding new contents, it should not be reflected in the copy
            writeToFile(aDir.resolve("file22"), "file22");
            writeToFile(bDir.resolve("file42"), "file42");
            writeToFile(cDir.resolve("file62"), "file62");
            writeToFile(dDir.resolve("file82"), "file82");

            {
                final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
                Files.walkFileTree(aDirCopy, concatFileVisitor);
                Collections.sort(concatFileVisitor.conents);

                Assert.assertEquals(
                        Arrays.asList(
                                "file1",
                                "file2",
                                "file3",
                                "file4",
                                "file5",
                                "file6",
                                "file7",
                                "file8"
                        ),
                        concatFileVisitor.conents);
            }

            // modify exiting contents, it should be reflected in the copy because it's a hard link
            writeToFile(aDir.resolve("file2"), "file2-modified");
            writeToFile(bDir.resolve("file4"), "file4-modified");
            writeToFile(cDir.resolve("file6"), "file6-modified");
            writeToFile(dDir.resolve("file8"), "file8-modified");

            {
                final ConcatFileVisitor concatFileVisitor2 = new ConcatFileVisitor();
                Files.walkFileTree(aDirCopy, concatFileVisitor2);
                Collections.sort(concatFileVisitor2.conents);

                Assert.assertEquals(
                        Arrays.asList(
                                "file1",
                                "file2-modified",
                                "file3",
                                "file4-modified",
                                "file5",
                                "file6-modified",
                                "file7",
                                "file8-modified"
                        ),
                        concatFileVisitor2.conents);
            }
        }
    }


    @Test
    public void testRecursiveCopy() throws IOException {
        final Path rootDir = tempDir.getRoot().toPath();
        final Path aDir = Files.createDirectory(rootDir.resolve("a"));
        writeToFile(aDir.resolve("file1"), "file1");
        writeToFile(aDir.resolve("file2"), "file2");

        final Path bDir = Files.createDirectory(aDir.resolve("b"));
        writeToFile(bDir.resolve("file3"), "file3");
        writeToFile(bDir.resolve("file4"), "file4");

        final Path cDir = Files.createDirectory(aDir.resolve("c"));
        writeToFile(cDir.resolve("file5"), "file5");
        writeToFile(cDir.resolve("file6"), "file6");

        final Path dDir = Files.createDirectory(cDir.resolve("d"));
        writeToFile(dDir.resolve("file7"), "file7");
        writeToFile(dDir.resolve("file8"), "file8");

        {
            final CountFileVisitor countFileVisitor = new CountFileVisitor();
            Files.walkFileTree(rootDir, countFileVisitor);

            Assert.assertEquals(5, countFileVisitor.dirs); // root, a, b, c, d
            Assert.assertEquals(8, countFileVisitor.files); // file1 - file8
        }

        {
            final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
            Files.walkFileTree(rootDir, concatFileVisitor);
            Collections.sort(concatFileVisitor.conents);

            Assert.assertEquals(
                    Arrays.asList(
                            "file1",
                            "file2",
                            "file3",
                            "file4",
                            "file5",
                            "file6",
                            "file7",
                            "file8"
                    ),
                    concatFileVisitor.conents);
        }

        {
            final Path aDirCopy = rootDir.resolve("acopy");
            PosixFileOperations.recursiveCopy(aDir, aDirCopy);

            {
                final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
                Files.walkFileTree(aDirCopy, concatFileVisitor);
                Collections.sort(concatFileVisitor.conents);

                Assert.assertEquals(
                        Arrays.asList(
                                "file1",
                                "file2",
                                "file3",
                                "file4",
                                "file5",
                                "file6",
                                "file7",
                                "file8"
                        ),
                        concatFileVisitor.conents);
            }

            // adding new contents modifying existing contents, it should not be reflected in the copy
            writeToFile(aDir.resolve("file22"), "file22");
            writeToFile(aDir.resolve("file2"), "file2-modified");

            {
                final ConcatFileVisitor concatFileVisitor = new ConcatFileVisitor();
                Files.walkFileTree(aDirCopy, concatFileVisitor);
                Collections.sort(concatFileVisitor.conents);

                Assert.assertEquals(
                        Arrays.asList(
                                "file1",
                                "file2",
                                "file3",
                                "file4",
                                "file5",
                                "file6",
                                "file7",
                                "file8"
                        ),
                        concatFileVisitor.conents);
            }
        }
    }
}