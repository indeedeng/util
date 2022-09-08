#ifndef CONFIG_H
#define CONFIG_H

#if defined(__APPLE__)
    #define HADOOP_ZLIB_LIBRARY "libz.1.dylib"
    #define HADOOP_SNAPPY_LIBRARY "libsnappy.1.dylib"
#else
    #define HADOOP_ZLIB_LIBRARY "libz.so.1"
    #define HADOOP_SNAPPY_LIBRARY "libsnappy.so"
#endif

#endif
