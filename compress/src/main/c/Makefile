#!/usr/bin/env make

.PHONY: all build clean

all: build

OS := $(shell uname -s | tr A-Z a-z)
ARCH ?= $(shell uname -m)
CC := gcc

#CFLAGS += -g -fPIC -O3 -Wall
CFLAGS += -fPIC -O3 -std=c99

# This relies on lazy evaluation of each variable since LIBTYPE is not set until later.
LIBNAME = libindeedcompress-$(ARCH).$(LIBTYPE)
TARGET = $(LIBNAME)

ifeq ($(OS), darwin)
	CFLAGS += -arch arm64 -arch x86_64 -target x86_64-apple-macos10.12
	LIBTYPE := dylib
	# Note that -Wl,-rpath,/opt/homebrew/lib instructs the linker to add
	#  the M1 homebrew install location to the search paths used by dlopen.
	LD_FLAGS += -dynamiclib -arch arm64 -arch x86_64 -Wl,-rpath,/opt/homebrew/lib
	DEST_DIR := "../resources/native/Mac OS X"
else
	DEST_DIR := "../resources/native/Linux-amd64"
	LIBTYPE := so
	LD_FLAGS += -shared
	#LD_FLAGS += -shared -Wl,-soname,$(LIBSONAME)
endif

mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
mkfile_dir := $(dir $(mkfile_path))
BUILD_DIR ?= $(realpath $(mkfile_dir)/../../../build)

INCLUDES += \
	-I$(BUILD_DIR)/generated/sources/headers/java/main \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/$(OS) \

OBJS = \
	SnappyCompressor-$(ARCH).o \
	SnappyDecompressor-$(ARCH).o \
	ZlibCompressor-$(ARCH).o \
	ZlibDecompressor-$(ARCH).o \

%-$(ARCH).o: %.c
	$(CC) $(CFLAGS) $(INCLUDES) -c -o $@ $?

$(LIBNAME): $(OBJS)
	$(CC) $(LD_FLAGS) -o $@ $(OBJS)

build: $(LIBNAME) 

$(DEST_DIR):
	mkdir -p $(DEST_DIR)

install: build $(DEST_DIR) 
	cp $(LIBNAME) $(DEST_DIR)/$(subst -$(ARCH),,$(LIBNAME))

clean: 
	rm -rf $(OBJS) $(TARGET)

