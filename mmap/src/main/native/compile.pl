#!/usr/bin/perl
`gcc -I$ENV{'JAVA_HOME'}/include/ -I$ENV{'JAVA_HOME'}/include/linux/ -c -fPIC com_indeed_util_mmap_MMapBuffer.c com_indeed_util_mmap_NativeMemoryUtils.c com_indeed_util_mmap_Stat.c`;
`gcc -shared -W1,-soname,libindeedmmap.so.1 -o libindeedmmap.so.1.0.1 com_indeed_util_mmap_MMapBuffer.o com_indeed_util_mmap_NativeMemoryUtils.o com_indeed_util_mmap_Stat.o`;
