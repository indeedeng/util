package com.indeed.util.mmap;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** @author jplaisance */
public final class LoadIndeedMMap {
    private static final Logger log = LoggerFactory.getLogger(LoadIndeedMMap.class);

    private static boolean loaded = false;

    public static synchronized void loadLibrary() {
        if (!loaded) {
            try {
                final String osName = System.getProperty("os.name");
                final String arch = System.getProperty("os.arch");
                final String extension = getLibraryType(osName);
                final String resourcePath =
                        "Mac OS X".equals(osName)
                                ? "/native/" + osName + "/libindeedmmap." + extension + ".1.0.1"
                                : "/native/"
                                        + osName
                                        + "-"
                                        + arch
                                        + "/libindeedmmap."
                                        + extension
                                        + ".1.0.1";
                final InputStream is = MMapBuffer.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new FileNotFoundException(
                            "unable to find libindeedmmap."
                                    + extension
                                    + ".1.0.1 at resource path "
                                    + resourcePath);
                }
                final File tempFile = File.createTempFile("libindeedmmap", "." + extension);
                final OutputStream os = new FileOutputStream(tempFile);
                ByteStreams.copy(is, os);
                os.close();
                is.close();
                System.load(tempFile.getAbsolutePath());
                // noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            } catch (Throwable e) {
                log.warn(
                        "unable to load libindeedmmap using class loader, looking in java.library.path",
                        e);
                System.loadLibrary("indeedmmap"); // if this fails it throws UnsatisfiedLinkError
            }
            loaded = true;
        }
    }

    // i only tested linux, the others are here just in case. i got them from
    // http://lopica.sourceforge.net/os.html
    static String getLibraryType(String os) {
        if (os.startsWith("Linux")) {
            return "so";
        }
        if (os.startsWith("FreeBSD")) {
            return "so";
        }
        if (os.startsWith("Mac OS X")) {
            return "dylib";
        }
        if (os.startsWith("Windows")) {
            return "dll";
        }
        throw new IllegalArgumentException();
    }
}
