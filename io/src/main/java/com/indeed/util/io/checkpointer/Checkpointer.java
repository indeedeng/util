package com.indeed.util.io.checkpointer;

import java.io.IOException;

/** @author tony */
public interface Checkpointer<T> {

    public T getCheckpoint();

    public void setCheckpoint(T checkpoint) throws IOException;
}
