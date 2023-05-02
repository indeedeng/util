# util-mmap

util-mmap is Indeed's memory-mapping library for Java. It provides an efficient
mechanism for accessing large files. Indeed's analytics platform [Imhotep](http://engineering.indeed.com/blog/2014/10/open-source-interactive-data-analytics-with-imhotep/)
uses it for managing data access.

## Installation

The latest util-mmap JAR file can be downloaded via Maven ([link](http://search.maven.org/#browse%7C-1269434767),
[link](http://mvnrepository.com/artifact/com.indeed/util-mmap)).

```
<dependencies>
    <dependency>
        <groupId>com.indeed</groupId>
        <artifactId>util-mmap</artifactId>
        <version>LATEST</version>
    </dependency>
    <!-- ... -->
</dependencies>
```

The library depends on [Unix mmap](http://www.gnu.org/software/libc/manual/html_node/Memory_002dmapped-I_002fO.html)
for its underlying functionality, so it uses JNI. The published JAR file contains 
native builds for Linux `aarch64`, Linux `amd64`, macOS `x86_64` and macOS `arm64`. The Java code in the library loads the 
correct Linux native shared object (.so, .dylib) file from the JAR file at runtime based
on `os.name` and `os.arch` system properties. If you need to run on an unsupported OS, you'll
need to rebuild the native code. See the instructions in [Building](#building).

## Usage

A good starting point for using util-mmap is the [MMapBuffer class](https://github.com/indeedeng/util/blob/main/mmap/src/main/java/com/indeed/util/mmap/MMapBuffer.java).
The following example maps in a large file containing an array of longs, written in little-endian order.

```
final MMapBuffer buffer = new MMapBuffer(
       filePath,
       FileChannel.MapMode.READ_ONLY,
       ByteOrder.LITTLE_ENDIAN);
final LongArray longArray =
    buffer.memory().longArray(0, buffer.memory().length() / 8);
final long firstValue = longArray.get(0);
```

## Building

You can build the native code for util-mmap yourself by executing the `updateNative` task. It compiles the native code
using the provided Makefile, which depends on GCC. On Linux you will need a 64 bit x86 or aarch64 environment, other architectures are not currently supported. On macOS, a universal dylib for both `x86_64` and `arm64`
is built automatically. Binaries are installed to `mmap/src/main/resources/`. for inclusion in the packaged jar.

For Linux if you are running on an x86 you will need to install "aarch64-linux-gnu-gcc" to build the AARCH64 library, if you are running on an AARCH64 you will need to install "x86_64-linux-gnu-gcc" to build the x86 compatible library 

The makefile can also be invoked directly with:

```
$ ./gradlew :mmap:compileJava
$ cd mmap/src/main/c/
$ make clean install
```
## Known limitations

The native code base has not been verified on other variants of Unix. For example,
`mremap` is unsupported for Darwin (OS X). Contributions welcome.

## License

[Apache License Version 2.0](https://github.com/indeedeng/util/blob/master/LICENSE)

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/indeedeng/util/mmap/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
