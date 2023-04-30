#!/usr/bin/env make

.PHONY: all build clean

all: build

OS := $(shell uname -s | tr A-Z a-z)
ARCH ?= $(shell uname -m)
REALARCH := $(shell uname -m)

CC ?= gcc

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
	ifeq ($(ARCH), x86_64)
		DEST_DIR := "../resources/native/Linux-amd64/"
	else
		DEST_DIR := "../resources/native/Linux-$(ARCH)/"
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

buildAARCH64OnX86:
	$(MAKE) CC=aarch64-linux-gnu-gcc ARCH=aarch64 build

buildX86OnAARCH64:
	$(MAKE) CC=x86_64-linux-gnu-gcc ARCH=x86_64 build

build: $(REALLIBNAME) $(if $(OS:linux=),,$(if $(ARCH:$(REALARCH)=),,$(if $(ARCH:x86_64=),buildX86OnAARCH64,buildAARCH64OnX86)))

install: build $(if $(OS:linux=),,$(if $(ARCH:$(REALARCH)=),,$(if $(ARCH:x86_64=),installX86,installAARCH64)))
	cp $(REALLIBNAME) $(DEST_DIR)/$(subst -$(ARCH),,$(REALLIBNAME))

installX86:
	$(MAKE) ARCH=x86_64 install

installAARCH64:
	$(MAKE) ARCH=aarch64 install

clean: $(if $(OS:linux=),,$(if $(ARCH:$(REALARCH)=),,$(if $(ARCH:x86_64=),cleanX86,cleanAARCH64)))
	rm -rf $(OBJS) $(TARGET)

cleanX86:
	$(MAKE) ARCH=x86_64 clean
cleanAARCH64:
	$(MAKE) ARCH=aarch64 clean

