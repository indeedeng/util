package com.indeed.util.io;

import java.io.Closeable;
import java.io.DataInput;

/**
 * @author jplaisance
 */
public interface RandomAccessDataInput extends DataInput, Seekable, Closeable {}
