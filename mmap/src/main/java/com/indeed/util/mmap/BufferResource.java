package com.indeed.util.mmap;

import java.io.Closeable;

/**
 * @author jplaisance
 */
public interface BufferResource extends Closeable {
    
    public Memory memory();
}
