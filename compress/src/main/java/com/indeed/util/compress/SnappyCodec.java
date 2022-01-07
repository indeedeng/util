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

import com.indeed.util.compress.snappy.SnappyCompressor;
import com.indeed.util.compress.snappy.SnappyDecompressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** This class creates snappy compressors/decompressors. */
public class SnappyCodec implements CompressionCodec {

    /** Are the native snappy libraries loaded &amp; initialized? */
    public static void checkNativeCodeLoaded() {
        if (!SnappyCompressor.isNativeCodeLoaded()) {
            throw new RuntimeException(
                    "native snappy library not available: "
                            + "SnappyCompressor has not been loaded.");
        }
        if (!SnappyDecompressor.isNativeCodeLoaded()) {
            throw new RuntimeException(
                    "native snappy library not available: "
                            + "SnappyDecompressor has not been loaded.");
        }
    }

    /**
     * Create a {@link CompressionOutputStream} that will write to the given {@link OutputStream}.
     *
     * @param out the location for the final output stream
     * @return a stream the user can write uncompressed data to have it compressed
     * @throws IOException
     */
    @Override
    public CompressionOutputStream createOutputStream(OutputStream out) throws IOException {
        return createOutputStream(out, createCompressor());
    }

    /**
     * Create a {@link CompressionOutputStream} that will write to the given {@link OutputStream}
     * with the given {@link Compressor}.
     *
     * @param out the location for the final output stream
     * @param compressor compressor to use
     * @return a stream the user can write uncompressed data to have it compressed
     * @throws IOException
     */
    @Override
    public CompressionOutputStream createOutputStream(OutputStream out, Compressor compressor)
            throws IOException {
        checkNativeCodeLoaded();
        final int bufferSize = 256 * 1024;

        final int compressionOverhead = (bufferSize / 6) + 32;

        return new BlockCompressorStream(out, compressor, bufferSize, compressionOverhead);
    }

    /**
     * Get the type of {@link Compressor} needed by this {@link CompressionCodec}.
     *
     * @return the type of compressor needed by this codec.
     */
    @Override
    public Class<? extends Compressor> getCompressorType() {
        checkNativeCodeLoaded();
        return SnappyCompressor.class;
    }

    /**
     * Create a new {@link Compressor} for use by this {@link CompressionCodec}.
     *
     * @return a new compressor for use by this codec
     */
    @Override
    public Compressor createCompressor() {
        checkNativeCodeLoaded();
        final int bufferSize = 256 * 1024;
        return new SnappyCompressor(bufferSize);
    }

    /**
     * Create a {@link CompressionInputStream} that will read from the given input stream.
     *
     * @param in the stream to read compressed bytes from
     * @return a stream to read uncompressed bytes from
     * @throws IOException
     */
    @Override
    public CompressionInputStream createInputStream(InputStream in) throws IOException {
        return createInputStream(in, createDecompressor());
    }

    /**
     * Create a {@link CompressionInputStream} that will read from the given {@link InputStream}
     * with the given {@link Decompressor}.
     *
     * @param in the stream to read compressed bytes from
     * @param decompressor decompressor to use
     * @return a stream to read uncompressed bytes from
     * @throws IOException
     */
    @Override
    public CompressionInputStream createInputStream(InputStream in, Decompressor decompressor)
            throws IOException {
        checkNativeCodeLoaded();
        return new BlockDecompressorStream(in, decompressor, 256 * 1024);
    }

    /**
     * Get the type of {@link Decompressor} needed by this {@link CompressionCodec}.
     *
     * @return the type of decompressor needed by this codec.
     */
    @Override
    public Class<? extends Decompressor> getDecompressorType() {
        checkNativeCodeLoaded();
        return SnappyDecompressor.class;
    }

    /**
     * Create a new {@link Decompressor} for use by this {@link CompressionCodec}.
     *
     * @return a new decompressor for use by this codec
     */
    @Override
    public Decompressor createDecompressor() {
        checkNativeCodeLoaded();
        final int bufferSize = 256 * 1024;
        return new SnappyDecompressor(bufferSize);
    }

    /**
     * Get the default filename extension for this kind of compression.
     *
     * @return <code>.snappy</code>.
     */
    @Override
    public String getDefaultExtension() {
        return ".snappy";
    }
}
