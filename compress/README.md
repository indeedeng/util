# util-compress

## About
util-compress is a library for compressing and uncompressing data. Includes snappy and gzip and a pluggable framework to support other codecs.

A binary is included for X86_64 and AARCH64 on both Linux and MacOS. Other platforms, including 32 bit platforms, will need to compile from source and may not compile or work as-is.

## Compiling native code on 64-bit linux

**Requires libsnappy-dev and libz-dev**

You can build the native code for compress yourself by executing the `updateNative` task. It compiles the native code
using the provided Makefile, which depends on GCC. 
For Linux if you are running on an x86 you will need to install "aarch64-linux-gnu-gcc" compiler, if you are running on an AARCH64 you will need to install "x86_64-linux-gnu-gcc" compiler. 

On macOS, a universal dylib for both `x86_64` and `arm64` is built automatically. Binaries are installed to `compress/src/main/resources/`. for inclusion in the packaged jar.

The makefile can also be invoked directly with:

```
$ ./gradlew :compress:compileJava
$ cd compress/src/main/c/
$ make clean install
```
