// Copyright 2015 Indeed
package com.indeed.util.io;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.apache.logging.log4j.core.appender.ConsoleAppender.Target;

/**
 * @author rboyer
 */
public class DirectoriesTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
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

    @BeforeClass
    public static void initClass() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        final Configuration config = builder.setStatusLevel(Level.WARN)
                                      .setConfigurationName("Indeed Logging")
                                      .add(builder.newAppender("stderr", "Console")
                                                  .addAttribute("target", Target.SYSTEM_ERR))
                                      .add(builder.newAsyncLogger("com.indeed", Level.DEBUG)
                                                  .add(builder.newAppenderRef("stderr"))
                                                  .addAttribute("addivity", false))
                                      .add(builder.newAsyncRootLogger(Level.WARN)
                                                  .add(builder.newAppenderRef("stderr"))
                                                  .addAttribute("additivity", false))
                                      .build();
        Configurator.initialize(builder.build());

    }
}
