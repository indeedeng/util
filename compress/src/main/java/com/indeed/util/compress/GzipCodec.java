/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.indeed.util.compress;

import com.indeed.util.compress.zlib.ZlibCompressor;
import com.indeed.util.compress.zlib.ZlibDecompressor;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/** This class creates gzip compressors/decompressors. */
public class GzipCodec implements CompressionCodec {
    /** A bridge that wraps around a DeflaterOutputStream to make it a CompressionOutputStream. */
    protected static class GzipOutputStream extends CompressorStream {

        private static class ResetableGZIPOutputStream extends GZIPOutputStream {

            public ResetableGZIPOutputStream(OutputStream out) throws IOException {
                super(out);
            }

            public void resetState() throws IOException {
                def.reset();
            }
        }

        public GzipOutputStream(OutputStream out) throws IOException {
            super(new ResetableGZIPOutputStream(out));
        }

        /**
         * Allow children types to put a different type in here.
         *
         * @param out the Deflater stream to use
         */
        protected GzipOutputStream(CompressorStream out) {
            super(out);
        }

        public void close() throws IOException {
            out.close();
        }

        public void flush() throws IOException {
            out.flush();
        }

        public void write(int b) throws IOException {
            out.write(b);
        }

        public void write(byte[] data, int offset, int length) throws IOException {
            out.write(data, offset, length);
        }

        public void finish() throws IOException {
            ((ResetableGZIPOutputStream) out).finish();
        }

        public void resetState() throws IOException {
            ((ResetableGZIPOutputStream) out).resetState();
        }
    }

    public CompressionOutputStream createOutputStream(OutputStream out) throws IOException {
        return new CompressorStream(out, createCompressor(), 4 * 1024);
    }

    public CompressionOutputStream createOutputStream(OutputStream out, Compressor compressor)
            throws IOException {
        return (compressor != null)
                ? new CompressorStream(out, compressor, 4 * 1024)
                : createOutputStream(out);
    }

    public Compressor createCompressor() {
        return new GzipZlibCompressor();
    }

    public Class<? extends Compressor> getCompressorType() {
        return GzipZlibCompressor.class;
    }

    public CompressionInputStream createInputStream(InputStream in) throws IOException {
        return createInputStream(in, null);
    }

    public CompressionInputStream createInputStream(InputStream in, Decompressor decompressor)
            throws IOException {
        if (decompressor == null) {
            decompressor = createDecompressor(); // always succeeds (or throws)
        }
        return new DecompressorStream(in, decompressor, 4 * 1024);
    }

    public Decompressor createDecompressor() {
        return new GzipZlibDecompressor();
    }

    public Class<? extends Decompressor> getDecompressorType() {
        return GzipZlibDecompressor.class;
    }

    public String getDefaultExtension() {
        return ".gz";
    }

    static final class GzipZlibCompressor extends ZlibCompressor {
        public GzipZlibCompressor() {
            super(
                    ZlibCompressor.CompressionLevel.DEFAULT_COMPRESSION,
                    ZlibCompressor.CompressionStrategy.DEFAULT_STRATEGY,
                    ZlibCompressor.CompressionHeader.GZIP_FORMAT,
                    64 * 1024);
        }
    }

    static final class GzipZlibDecompressor extends ZlibDecompressor {
        public GzipZlibDecompressor() {
            super(ZlibDecompressor.CompressionHeader.AUTODETECT_GZIP_ZLIB, 64 * 1024);
        }
    }
}
