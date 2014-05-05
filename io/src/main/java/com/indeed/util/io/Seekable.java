package com.indeed.util.io;


import java.io.IOException;

/**
 * @author jplaisance
 */
public interface Seekable extends Positioned {
    public void seek(long position) throws IOException;

    public long length() throws IOException;
}
