package com.indeed.util.io;


import java.io.IOException;

/**
 * @author jplaisance
 */
public interface Syncable {
    public void sync() throws IOException;
}
