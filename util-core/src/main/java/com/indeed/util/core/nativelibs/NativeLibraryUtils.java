package com.indeed.util.core.nativelibs;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jplaisance
 */
public final class NativeLibraryUtils {

    private static final Logger log = LoggerFactory.getLogger(NativeLibraryUtils.class);

    public static void loadLibrary(String name) {
        loadLibrary(name, "");
    }

    public static void loadLibrary(String name, String version) {
        try {
            final String osName = System.getProperty("os.name");
            final String arch = System.getProperty("os.arch");
            final String prefix = getLibraryPrefix(osName);
            final String extension = getLibraryType(osName);
            final String libName = prefix+name+"."+extension+(Strings.isNullOrEmpty(version) ? "" : "."+version);
            final String resourcePath = "/native/" + osName + "-" + arch + "/" + libName;
            final InputStream is = NativeLibraryUtils.class.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new FileNotFoundException("unable to find "+libName+" at resource path "+resourcePath);
            }
            final File tempFile = File.createTempFile(prefix+name, "."+extension);
            final OutputStream os = new FileOutputStream(tempFile);
            ByteStreams.copy(is, os);
            os.close();
            is.close();
            System.load(tempFile.getAbsolutePath());
            // noinspection ResultOfMethodCallIgnored
            tempFile.delete();
        } catch (Throwable e) {
            log.warn("unable to load "+name+" using class loader, looking in java.library.path", e);
            System.loadLibrary(name); // if this fails it throws UnsatisfiedLinkError
        }
    }

    static String getLibraryPrefix(String os) {
        if (os.startsWith("Windows")) {
            return "";
        }
        return "lib";
    }

    //i only tested linux, the others are here just in case. i got them from http://lopica.sourceforge.net/os.html
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
