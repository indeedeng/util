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
REALLIBNAME = $(LIBSONAME).0.3

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
	ifeq ($(ARCH), $(REALARCH))
		ifeq ($(ARCH), x86_64)
			ADDITIONAL_ARCH := aarch64
			ADDITIONAL_CC := aarch64-linux-gnu-gcc
		else
			ADDITIONAL_ARCH := x86_64
			ADDITIONAL_CC := x86_64-linux-gnu-gcc
		endif
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

build: $(REALLIBNAME) $(if $(ADDITIONAL_ARCH),buildAdditional)

buildAdditional:
	$(MAKE) CC=$(ADDITIONAL_CC) ARCH=$(ADDITIONAL_ARCH)

install: build $(if $(ADDITIONAL_ARCH),installAdditional)
	mkdir -p $(DEST_DIR)
	cp $(REALLIBNAME) $(DEST_DIR)/$(subst -$(ARCH),,$(REALLIBNAME))

installAdditional:
	$(MAKE) ARCH=$(ADDITIONAL_ARCH) install

clean: $(if $(ADDITIONAL_ARCH),cleanAdditional)
	rm -rf $(OBJS) $(TARGET)
cleanAdditional:
	$(MAKE) ARCH=$(ADDITIONAL_ARCH) clean
