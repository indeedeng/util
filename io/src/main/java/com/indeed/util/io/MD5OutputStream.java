package com.indeed.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** @author mmorrison */
public class MD5OutputStream extends FilterOutputStream {
    MessageDigest md5;

    public MD5OutputStream(OutputStream out) {
        super(out);
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // this is... not even possible.
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        md5.update((byte) b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        md5.update(b, off, len);
    }

    public String getHashString() {
        String hash = new BigInteger(1, md5.digest()).toString(16);
        while (hash.length() < 32) {
            hash = "0" + hash;
        }
        return hash;
    }
}
