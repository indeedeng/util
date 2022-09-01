#include <string.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <errno.h>
#include "com_indeed_util_mmap_NativeMemoryUtils.h"

/*
 * Class:     com_indeed_util_mmap_NativeMemoryUtils
 * Method:    copyToDirectBuffer
 * Signature: (JLjava/nio/ByteBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_copyToDirectBuffer(JNIEnv* env, jclass class, jlong srcAddr, jobject dest, jint offset, jint length) {
    memcpy((*env)->GetDirectBufferAddress(env, dest)+offset, (void*)srcAddr, length);
}

/*
 * Class:     com_indeed_util_mmap_NativeMemoryUtils
 * Method:    copyFromDirectBuffer
 * Signature: (Ljava/nio/ByteBuffer;JI)V
 */
JNIEXPORT void JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_copyFromDirectBuffer(JNIEnv* env, jclass class, jobject src, jint offset, jlong destAddr, jint length) {
    memcpy((void*)destAddr, (*env)->GetDirectBufferAddress(env, src)+offset, length);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    copyFromByteArray
 * Signature: ([BIIJ)I
 */
JNIEXPORT void JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_copyFromByteArray (JNIEnv* env, jclass class, jbyteArray bytes, jint offset, jint length, jlong address) {
    (*env)->GetByteArrayRegion(env, bytes, offset, length, (jbyte*)address);
}

/*
 * Class:     com_indeed_util_mmap_MMapBuffer
 * Method:    copyToByteArray
 * Signature: (JI[BI)I
 */
JNIEXPORT void JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_copyToByteArray (JNIEnv* env, jclass class, jlong address, jint length, jbyteArray bytes, jint offset) {
    (*env)->SetByteArrayRegion(env, bytes, offset, length, (jbyte*)address);
}

JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_mlock0 (JNIEnv* env, jclass class, jlong addr, jlong len) {
    int err;
    err = mlock((void*)addr, len);
    if (err != 0) {
        err = errno;
        if (err == ENOMEM) return com_indeed_util_mmap_NativeMemoryUtils_ENOMEM;
        if (err == EPERM) return com_indeed_util_mmap_NativeMemoryUtils_EPERM;
        if (err == EAGAIN) return com_indeed_util_mmap_NativeMemoryUtils_EAGAIN;
    }
    return err;
}

JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_munlock0 (JNIEnv* env, jclass class, jlong addr, jlong len) {
    int err;
    err = munlock((void*)addr, len);
    if (err != 0) {
        err = errno;
        if (err == ENOMEM) return com_indeed_util_mmap_NativeMemoryUtils_ENOMEM;
        if (err == EAGAIN) return com_indeed_util_mmap_NativeMemoryUtils_EAGAIN;
    }
    return err;
}

/*
 * Class:     com_indeed_util_mmap_NativeMemoryUtils
 * Method:    mincore
 * Signature: (JJJ)I
 */
JNIEXPORT jint JNICALL Java_com_indeed_util_mmap_NativeMemoryUtils_mincore(JNIEnv* env, jclass class, jlong addr, jlong length, jlong vec) {
    int err;
    err = mincore((caddr_t)addr, length, (char*)vec);
    if (err != 0) {
        err = errno;
        if (err == ENOMEM) return com_indeed_util_mmap_NativeMemoryUtils_ENOMEM;
        if (err == EFAULT) return com_indeed_util_mmap_NativeMemoryUtils_EFAULT;
        if (err == EINVAL) return com_indeed_util_mmap_NativeMemoryUtils_EINVAL;
        if (err == EAGAIN) return com_indeed_util_mmap_NativeMemoryUtils_EAGAIN;
    }
    return err;
}
