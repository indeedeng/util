#!/usr/bin/env make

.PHONY: all build clean

all: build

OS := $(shell uname -s | tr A-Z a-z)
ARCH ?= $(shell uname -m)
CC := gcc

CFLAGS += -g -fPIC -O3 -Wall

# These rely on lazy evaluation of each variable since LIBTYPE is not set until later.
LIBNAME = libindeedmmap-$(ARCH).$(LIBTYPE)
LIBSONAME = $(LIBNAME).1
REALLIBNAME = $(LIBSONAME).0.1

TARGET = $(REALLIBNAME)

ifeq ($(OS), darwin)
	CFLAGS += -arch arm64 -arch x86_64 -target x86_64-apple-macos10.12
	LIBTYPE := dylib
	LD_FLAGS += -dynamiclib -arch arm64 -arch x86_64
	DEST_DIR := "../resources/native/Mac OS X/"
else
	ifneq ($(ARCH), x86_64)
		CFLAGS += -m32
		LD_FLAGS += -m32
		DEST_DIR := "../resources/native/Linux-i386/"
	else
		DEST_DIR := "../resources/native/Linux-amd64/"
	endif
	LIBTYPE := so
	LD_FLAGS += -shared -Wl,-soname,$(LIBSONAME)
endif

mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
mkfile_dir := $(dir $(mkfile_path))
BUILD_DIR ?= $(realpath $(mkfile_dir)/../../../build)

INCLUDES += \
	-I$(BUILD_DIR)/generated/sources/headers/java/main \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/$(OS) \

OBJS = \
	com_indeed_util_mmap_MMapBuffer-$(ARCH).o \
	com_indeed_util_mmap_NativeMemoryUtils-$(ARCH).o \
	com_indeed_util_mmap_Stat-$(ARCH).o \

%-$(ARCH).o: %.c
	$(CC) $(CFLAGS) $(INCLUDES) -c -o $@ $?

$(REALLIBNAME): $(OBJS)
	$(CC) $(LD_FLAGS) -o $@ $(OBJS)

build: $(REALLIBNAME) $(if $(OS:linux=),,$(if $(ARCH:x86_64=),,build32))

build32:
	$(MAKE) ARCH=i386 build

install: build $(if $(OS:linux=),,$(if $(ARCH:x86_64=),,install32))
	cp $(REALLIBNAME) $(DEST_DIR)/$(subst -$(ARCH),,$(REALLIBNAME))

install32:
	$(MAKE) ARCH=i386 install

clean: $(if $(OS:linux=),,$(if $(ARCH:x86_64=),,clean32))
	rm -rf $(OBJS) $(TARGET)

clean32:
	$(MAKE) ARCH=i386 clean
