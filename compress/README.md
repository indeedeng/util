# util-compress

## About
util-compress is a library for compressing and uncompressing data. Includes snappy and gzip.

A binary is included for Linux-amd64. It will not work on 32-bit platforms. Other platforms will need to compile from source and may not compile or work as-is.

## Compiling native code on 64-bit linux

```
# from the util-compress project root
rm src/main/native/com_indeed_util_compress_*.h

javah -classpath target/classes -d src/main/native/ com.indeed.util.compress.snappy.SnappyCompressor com.indeed.util.compress.snappy.SnappyDecompressor com.indeed.util.compress.zlib.ZlibCompressor com.indeed.util.compress.zlib.ZlibDecompressor

cd src/native

gcc -c -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -O3 -fPIC -std=c99 *.c

gcc -fPIC -shared -O3 *.o -o libindeedcompress.so

mv libindeedcompress.so ../resources/native/Linux-amd64/

rm *.o
```