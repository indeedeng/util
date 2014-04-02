#include "com_indeed_squall_mmap_Stat.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <stdint.h>

int j_errno(int err) {
	switch (err) {
		case EACCES : return 1;
		case EBADF : return 2;
		case EFAULT : return 3;
		case ELOOP : return 4;
		case ENAMETOOLONG : return 5;
		case ENOENT : return 6;
		case ENOMEM : return 7;
		case ENOTDIR : return 8;
		case EOVERFLOW : return 9;
		default : return -1;
	}
}

void copy_to_addr(struct stat* statbuf, void* addr) {
	memset(addr, 0, 92);
	*((uint64_t*)(addr+0)) = statbuf->st_dev;
	*((uint64_t*)(addr+8)) = statbuf->st_ino;
	*((uint32_t*)(addr+16)) = statbuf->st_mode;
	*((uint64_t*)(addr+20)) = statbuf->st_nlink;
	*((uint32_t*)(addr+28)) = statbuf->st_uid;
	*((uint32_t*)(addr+32)) = statbuf->st_gid;
	*((uint64_t*)(addr+36)) = statbuf->st_rdev;
	*((uint64_t*)(addr+44)) = statbuf->st_size;
	*((uint64_t*)(addr+52)) = statbuf->st_blksize;
	*((uint64_t*)(addr+60)) = statbuf->st_blocks;
	*((uint64_t*)(addr+68)) = statbuf->st_atime;
	*((uint64_t*)(addr+76)) = statbuf->st_mtime;
	*((uint64_t*)(addr+84)) = statbuf->st_ctime;
}

JNIEXPORT jint JNICALL Java_com_indeed_squall_mmap_Stat_stat(JNIEnv* env, jclass class, jstring jpath, jlong addr) {
	struct stat statbuf;
	char* path;
	int err;
	path = (char*)(*env)->GetStringUTFChars(env, jpath, NULL);
	err = stat(path, &statbuf);
	if (err != 0) {
		return j_errno(errno);
	}
	(*env)->ReleaseStringUTFChars(env, jpath, path);
	copy_to_addr(&statbuf, (void*)addr);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_indeed_squall_mmap_Stat_lstat(JNIEnv* env, jclass class, jstring jpath, jlong addr) {
	struct stat statbuf;
	char* path;
	int err;
	path = (char*)(*env)->GetStringUTFChars(env, jpath, NULL);
	err = lstat(path, &statbuf);
	if (err != 0) {
		return j_errno(errno);
	}
	(*env)->ReleaseStringUTFChars(env, jpath, path);
	copy_to_addr(&statbuf, (void*)addr);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_indeed_squall_mmap_Stat_fstat(JNIEnv* env, jclass class, jint fd, jlong addr) {
	struct stat statbuf;
	int err;
	err = fstat(fd, &statbuf);
	if (err != 0) {
		return j_errno(errno);
	}
	copy_to_addr(&statbuf, (void*)addr);
	return 0;
}
