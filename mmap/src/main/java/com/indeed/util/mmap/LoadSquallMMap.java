package com.indeed.util.mmap;

import com.google.common.io.ByteStreams;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jplaisance
 */
public final class LoadSquallMMap {
    private static final Logger log = Logger.getLogger(LoadSquallMMap.class);

    private static boolean loaded = false;

    public static synchronized void loadLibrary() {
        if (!loaded) {
            try {
                final String osName = System.getProperty("os.name");
                final String arch = System.getProperty("os.arch");
                final String resourcePath = "/native/" + osName + "-" + arch + "/libsquallmmap.so.1.0.1";
                final InputStream is = MMapBuffer.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new FileNotFoundException("unable to find libsquallmmap.so.1.0.1 at resource path "+resourcePath);
                }
                final File tempFile = File.createTempFile("libsquallmmap", ".so");
                final OutputStream os = new FileOutputStream(tempFile);
                ByteStreams.copy(is, os);
                os.close();
                is.close();
                System.load(tempFile.getAbsolutePath());
                // noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            } catch (Throwable e) {
                log.warn("unable to load libsquallmmap using class loader, looking in java.library.path", e);
                System.loadLibrary("squallmmap"); // if this fails it throws UnsatisfiedLinkError
            }
            loaded = true;
        }
    }
}
