// Copyright 2015 Indeed
package com.indeed.util.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/** @author rboyer */
public class DirectoriesTest {
    @Rule public TemporaryFolder tempDir = new TemporaryFolder();
    private Path root;

    @Test
    public void count() throws IOException {
        Assert.assertEquals(0, Directories.count(root));

        tempDir.newFile("foo1");

        Assert.assertEquals(1, Directories.count(root));

        final File dir = tempDir.newFolder("dir");

        Assert.assertEquals(2, Directories.count(root));

        Assert.assertTrue(new File(dir, "foo2").createNewFile());

        // files under a dir do not count
        Assert.assertEquals(2, Directories.count(root));
    }

    @Test
    public void list() throws IOException {
        List<Path> found;

        found = Directories.list(root);
        Assert.assertEquals(0, found.size());

        tempDir.newFile("foo1");

        found = Directories.list(root);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(root.resolve("foo1"), found.get(0));

        final File dir = tempDir.newFolder("dir");

        found = Directories.list(root);
        Assert.assertEquals(2, found.size());
        Collections.sort(found);
        Assert.assertEquals(root.resolve("dir"), found.get(0));
        Assert.assertEquals(root.resolve("foo1"), found.get(1));

        Assert.assertTrue(new File(dir, "foo2").createNewFile());

        // files under a dir do not count
        found = Directories.list(root);
        Assert.assertEquals(2, found.size());
        Collections.sort(found);
        Assert.assertEquals(root.resolve("dir"), found.get(0));
        Assert.assertEquals(root.resolve("foo1"), found.get(1));
    }

    @Before
    public void init() {
        root = tempDir.getRoot().toPath();
    }
}
