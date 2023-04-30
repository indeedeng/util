# util-compress

## About
util-compress is a library for compressing and uncompressing data. Includes snappy and gzip.

A binary is included for Linux-amd64 and Linux-aarch64. It will not work on 32-bit platforms. Other platforms will need to compile from source and may not compile or work as-is.

## Compiling native code on 64-bit linux

**Requires libsnappy-dev and libz-dev**

You can build the native code for compress yourself by executing the `updateNative` task. It compiles the native code
using the provided Makefile, which depends on GCC. 
For Linux if you are running on an x86 and you wish to build the ARM compatible library, you will need to install "aarch64-linux-gnu-gcc", if you are running on an AARCH64 and wish to build the x86 version of the library you will need to install "x86_64-linux-gnu-gcc". If you do not wish to cross-compile for local testing you can invoke the buildX86/buildAARCH64/installX86/installAARCH64 targets to build just for your current architecture. However we ask when comitting changes that modify the C code that you rebuild for all the supported architectures. 

On macOS, a universal dylib for both `x86_64` and `arm64` is built automatically. Binaries are installed to `compress/src/main/resources/`. for inclusion in the packaged jar.

The makefile can also be invoked directly with:

```
$ ./gradlew :compress:compileJava
$ cd compress/src/main/c/
$ make clean install
```
