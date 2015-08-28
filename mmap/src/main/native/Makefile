.PHONY: all build clean

all: build

OS := $(shell uname -s | tr A-Z a-z)
ARCH := $(shell uname -m)
CC := gcc

CFLAGS += -g -fPIC -O3 -Wall

ifneq ($(ARCH), x86_64)
	CFLAGS += -m32
endif

ifeq ($(OS),darwin)
	LIBTYPE := dylib
	LD_FLAGS += -dynamiclib
else
	LIBTYPE := so
	LD_FLAGS += -shared
endif

INCLUDES += \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/$(OS) \

LIBNAME = libindeedmmap.$(LIBTYPE)
LIBSONAME := $(LIBNAME).1
REALLIBNAME = $(LIBSONAME).0.1

TARGET = $(REALLIBNAME)

OBJS = \
	com_indeed_util_mmap_MMapBuffer.o \
	com_indeed_util_mmap_NativeMemoryUtils.o \
	com_indeed_util_mmap_Stat.o \

%.o: %.c
	$(CC) $(CFLAGS) $(INCLUDES) -c -o $@ $?

$(REALLIBNAME): $(OBJS)
	$(CC) $(LD_FLAGS) -W1,-soname,$(LIBSONAME) -o $@ $(OBJS)

build: $(REALLIBNAME)

install: $(REALLIBNAME)
	ln -sf $(PWD)/$(REALLIBNAME) /usr/lib/$(LIBNAME)

clean:
	rm -rf $(OBJS) $(TARGET)
