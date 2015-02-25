# util-mmap

util-mmap is Indeed's memory-mapping library for Java. It provides an efficient
mechanism for accessing large files. Indeed's analytics platform [Imhotep](http://engineering.indeed.com/blog/2014/10/open-source-interactive-data-analytics-with-imhotep/)
uses it for managing data access.

## Installation

The latest util-mmap JAR file can be downloaded via Maven ([link](http://search.maven.org/#browse%7C-1269434767),
[link](http://mvnrepository.com/artifact/com.indeed/util-mmap)).

The library depends on [Unix mmap](http://www.gnu.org/software/libc/manual/html_node/Memory_002dmapped-I_002fO.html)
for its underlying functionality, so it uses JNI. The published JAR file contains 
native builds for i386 and amd64. This example installs the amd64 library in /usr/lib:

```
$ jar xvf util-mmap-1.0.12.jar native
  created: native/
  created: native/Linux-i386/
  created: native/Linux-amd64/
 inflated: native/Linux-i386/libindeedmmap.so.1.0.1
 inflated: native/Linux-amd64/libindeedmmap.so.1.0.1

$ sudo cp -p native/Linux-amd64/libindeedmmap.so.1.0.1 /usr/lib/

$ sudo ln -sf /usr/lib/libindeedmmap.so.1.0.1 /usr/lib/libindeedmmap.so
```

You may also build the native code yourself, see instructions below.

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

Distributing a new build binary in the JAR file requires pushing it
into the GitHub repository under src/main/resources/native/*platform*/.

### Known limitations

The native code base has not been verified on other variants of Unix. For example,
there are known problems compiling for Darwin (OS X). Contributions welcome.

# License

[Apache License Version 2.0](https://github.com/indeedeng/util/blob/master/LICENSE)

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/indeedeng/util/mmap/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
