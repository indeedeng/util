package com.indeed.util.core;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/** @author jplaisance */
public final class NativeLibraryUtils {

    private static final Logger log = LoggerFactory.getLogger(NativeLibraryUtils.class);

    public static void loadLibrary(final String name) {
        loadLibrary(name, "");
    }

    public static void loadLibrary(final String name, final String version) {
        try {
            final String osName = System.getProperty("os.name");
            final String arch = System.getProperty("os.arch");
            final String prefix = getLibraryPrefix(osName);
            final String extension = getLibraryType(osName);
            final String libName =
                    prefix
                            + name
                            + "."
                            + extension
                            + (Strings.isNullOrEmpty(version) ? "" : "." + version);
            if (tryLoad(
                    "/native/" + osName + "-" + arch + "/" + libName, prefix, name, extension)) {
                return;
            }
            if ("Mac OS X".equals(osName)) {
                if (tryLoad("/native/" + osName + "/" + libName, prefix, name, extension)) {
                    return;
                }
            }
        } catch (final Exception | UnsatisfiedLinkError e) {
            log.warn("unable to load {} using class loader, looking in java.library.path", name, e);
        }
        System.loadLibrary(name); // if this fails it throws UnsatisfiedLinkError
    }

    private static boolean tryLoad(
            final String resourcePath,
            final String prefix,
            final String name,
            final String extension)
            throws IOException {
        try (final InputStream is = NativeLibraryUtils.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.debug("Cannot find library at {}", resourcePath);
                return false;
            }
            final File tempFile = File.createTempFile(prefix + name, "." + extension);
            try (final OutputStream os = Files.newOutputStream(tempFile.toPath())) {
                final byte[] buf = new byte[8192];
                while (true) {
                    final int r = is.read(buf);
                    if (r == -1) {
                        break;
                    }
                    os.write(buf, 0, r);
                }
                System.load(tempFile.getAbsolutePath());
                return true;
            } finally {
                // noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        }
    }

    static String getLibraryPrefix(final String os) {
        if (os.startsWith("Windows")) {
            return "";
        }
        return "lib";
    }

    // i only tested linux, the others are here just in case. i got them from
    // http://lopica.sourceforge.net/os.html
    static String getLibraryType(final String os) {
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
