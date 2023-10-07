#define _GNU_SOURCE

#include <sys/mman.h>
#include <errno.h>
#include "com_indeed_util_mmap_MMapBuffer.h"

// MAP_ANON for OSX
#ifndef MAP_ANONYMOUS
  #ifdef MAP_ANON
    #define MAP_ANONYMOUS MAP_ANON
  #endif
#endif

int get_c_flags(int flags) {
    int c_flags = 0;
    if (flags & com_indeed_util_mmap_MMapBuffer_MAP_SHARED) {
        c_flags |= MAP_SHARED;
    }
    if (flags & com_indeed_util_mmap_MMapBuffer_MAP_PRIVATE) {
        c_flags |= MAP_PRIVATE;
    }
    if (flags & com_indeed_util_mmap_MMapBuffer_MAP_ANONYMOUS) {
        c_flags |= MAP_ANONYMOUS;
    }
    return c_flags;
}

/*
 * Class:     com_indeed_squall_mmap_MMapBuffer
 * Method:    extractFd
 * Signature: (J)I
 * See: https://www.kfu.com/~nsayer/Java/jni-filedesc.html
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_extractFd(JNIEnv* env, jclass class, jobject fileDesciptor) {
    jfieldID field_fd;
    jclass class_fdesc;

    // FindClass and GetFieldID will throw the appropriate exceptions so the return values here are irrelevant.
    class_fdesc = (*env)->FindClass(env, "java/io/FileDescriptor");
    if (class_fdesc == NULL) {
        return -1;
    }

    field_fd = (*env)->GetFieldID(env, class_fdesc, "fd", "I");
    if (field_fd == NULL) {
        return -1;
    }

    return (*env)->GetIntField(env, fileDesciptor, field_fd);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    mmap
 * Signature: (JIIJ)J
 */
JNIEXPORT jlong JNICALL Java_com_indeed_util_mmap_MMapBuffer_mmap (JNIEnv* env, jclass class, jlong length, jint prot, jint flags, jint fd, jlong offset) {
    void* map_addr;
    int c_flags;
    c_flags = get_c_flags(flags);
    if (prot == com_indeed_util_mmap_MMapBuffer_READ_ONLY) {
        map_addr = mmap(0, length, PROT_READ, c_flags, fd, offset);
    } else if (prot == com_indeed_util_mmap_MMapBuffer_READ_WRITE) {
        map_addr = mmap(0, length, PROT_READ|PROT_WRITE, c_flags, fd, offset);
    } else {
        // this case will never trigger because the java code will only ever call this method with READ_ONLY or READ_WRITE
        return com_indeed_util_mmap_MMapBuffer_MAP_FAILED;
    }
    if (map_addr == MAP_FAILED) {
        return com_indeed_util_mmap_MMapBuffer_MAP_FAILED;
    }
    return (jlong)map_addr;
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    munmap
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_munmap (JNIEnv* env, jclass class, jlong address, jlong length) {
    return munmap((void*)address, length);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    mremap
 * Signature: (JJJI)J
 */
JNIEXPORT jlong JNICALL Java_com_indeed_util_mmap_MMapBuffer_mremap (JNIEnv* env, jclass class, jlong address, jlong oldSize, jlong newSize) {
    void* map_addr;
#ifdef MREMAP_MAYMOVE
    map_addr = mremap((void*)address, oldSize, newSize, MREMAP_MAYMOVE);
#else
    // mremap not supported on OSX
    return com_indeed_util_mmap_MMapBuffer_MAP_FAILED;
#endif
    if (map_addr == MAP_FAILED) {
        return com_indeed_util_mmap_MMapBuffer_MAP_FAILED;
    }
    return (jlong)map_addr;
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    msync
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_msync (JNIEnv* env, jclass class, jlong address, jlong length) {
    return msync((void*)address, length, MS_SYNC);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    madvise
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_madvise (JNIEnv* env, jclass class, jlong address, jlong length) {
    return madvise((void*)address, length, MADV_WILLNEED);
}

JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_posix_madvise (JNIEnv* env, jclass class, jlong address, jlong length, jint advice) {
    return madvise((void*)address, length, advice);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    madviseDontNeed
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_madviseDontNeed (JNIEnv* env, jclass class, jlong address, jlong length) {
    return madvise((void*)address, length, MADV_DONTNEED);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    errno
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_MMapBuffer_errno (JNIEnv* env, jclass class) {
    return errno;
}
