package com.indeed.util.io;

import java.io.IOException;

/** @author jplaisance */
public interface Positioned {
    public long position() throws IOException;
}
