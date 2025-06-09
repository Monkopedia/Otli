SOURCES := $(wildcard src/*/*.kt) $(wildcard src/*/*/*.kt)
OUTPUT_DIR := build/
OTLIC := ../otlic.sh

.PHONY: stdlib all clean

all: stdlib

stdlib: build/stdlib.klib

clean:
	rm -rf build

build/stdlib.klib: $(SOURCES)
	$(OTLIC) -Xallow-kotlin-package -Xmultifile-parts-inherit -verbose -output-klib -ir-output-dir build/ -ir-output-name stdlib -Xstdlib-compilation -Xdont-warn-on-error-suppression $^
