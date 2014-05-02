package com.indeed.util.compress;

import com.indeed.util.core.NativeLibraryUtils;
import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class NativeCodeLoader {
    private static final Logger log = Logger.getLogger(NativeCodeLoader.class);

    static boolean nativeCodeLoaded = false;

    static {
        if (!"64".equals(System.getProperty("sun.arch.data.model"))) {
            throw new RuntimeException("Indeed compress only works on the 64 bit jvm");
        }
        NativeLibraryUtils.loadLibrary("indeedcompress");
        nativeCodeLoaded = true;
    }

    public static boolean isNativeCodeLoaded() {
        return nativeCodeLoaded;
    }
}
