package com.indeed.util.io;

import java.io.ByteArrayOutputStream;

/**
 * can directly examine the internal byte[] backing this stream, use in conjuction with {@link
 * #size()} to get the logical length of the internal byte[] buffer
 *
 * @author rboyer
 */
public class IndexableByteArrayOutputStream extends ByteArrayOutputStream {
    public IndexableByteArrayOutputStream(int size) {
        super(size);
    }

    public byte[] unsafeByteArrayView() {
        return buf;
    }
}
