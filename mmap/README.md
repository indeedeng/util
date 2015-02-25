# util-mmap

util-mmap is Indeed's memory-mapping library for Java. It provides an efficient
mechanism for accessing large files. Indeed's analytics platform [Imhotep](http://engineering.indeed.com/blog/2014/10/open-source-interactive-data-analytics-with-imhotep/)
uses it for managing data access.

## Installation

The latest util-mmap JAR file can be downloaded via Maven ([link](http://search.maven.org/#browse%7C-1269434767),
[link](http://mvnrepository.com/artifact/com.indeed/util-mmap)).

The library depends on [Unix mmap](http://www.gnu.org/software/libc/manual/html_node/Memory_002dmapped-I_002fO.html)
for its underlying functionality, so it uses JNI. The published JAR file contains 
native builds for linux i386 and amd64. The Java code in the library loads the 
correct linux native shared object (.so) files from the JAR file at runtime based
on `os.name` and `os.arch` system properties. If you need to run on an unsupported OS, you'll
need to rebuild the native code; see the instructions in the Building section below.

## Usage

A good starting point for using util-mmap is the [MMapBuffer class](https://eng-git.ausoff.indeed.net/opensource/util/blob/master/mmap/src/main/java/com/indeed/util/mmap/MMapBuffer.java).
This example maps in a large file containing an array of longs, written in little-endian order:

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

You can build the native code for util-mmap yourself using the provided Perl
scripts, which are Linux-specific and depend on gcc:

```
$ cd util/mmap/src/main/native/

$ ./compile.pl

$ ls *.o *.so.*
com_indeed_util_mmap_MMapBuffer.o	  com_indeed_util_mmap_Stat.o
com_indeed_util_mmap_NativeMemoryUtils.o  libindeedmmap.so.1.0.1

$ sudo ./install.pl

$ ls -l /usr/lib/libindeedmmap.so
lrwxrwxrwx 1 root root 66 Feb 25 12:52 /usr/lib/libindeedmmap.so -> 
  /home/user/util/mmap/src/main/native/libindeedmmap.so.1.0.1
```

The installation step assumes that `/usr/lib/` is in your `java.library.path`.
You can also repackage the shared library in the JAR file, or request that we
distribute a new build binary in the JAR file requires pushing it
into the GitHub repository under src/main/resources/native/*platform*/.

### Known limitations

The native code base has not been verified on other variants of Unix. For example,
there are known problems compiling for Darwin (OS X). Contributions welcome.

# License

[Apache License Version 2.0](https://github.com/indeedeng/util/blob/master/LICENSE)

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/indeedeng/util/mmap/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
