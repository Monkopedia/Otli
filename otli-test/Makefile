SOURCES := $(wildcard src/kotlin/*/*.kt) $(wildcard src/kotlin/*/*/*.kt)
OUTPUT_DIR := build/
OTLIC := ../otlic.sh

.PHONY: otli-test all clean

all: otli-test

otli-test: build/otli-test.klib

clean:
	rm -rf build

build/otli-test.klib: $(SOURCES)
	#  -Xstdlib-compilation
	$(OTLIC) -kotlin-home /usr/share/kotlin -output-klib -verbose -ir-output-dir build/ -ir-output-name otli-test -libraries=../otli-stdlib/build/otli-stdlib.klib $^
