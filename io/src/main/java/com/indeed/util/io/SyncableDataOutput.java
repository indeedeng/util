package com.indeed.util.io;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.Flushable;

/** @author jplaisance */
public interface SyncableDataOutput
        extends DataOutput, Syncable, Positioned, Closeable, Flushable {}
