package com.indeed.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** @author mmorrison */
public class MD5InputStream extends FilterInputStream {
    MessageDigest md5;

    public MD5InputStream(InputStream in) {
        super(in);
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // this is... not even possible.
        }
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) md5.update((byte) b);
        return b;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            md5.update(b, off, result);
        }
        return result;
    }

    public String getHashString() {
        String hash = new BigInteger(1, md5.digest()).toString(16);
        while (hash.length() < 32) {
            hash = "0" + hash;
        }
        return hash;
    }
}
