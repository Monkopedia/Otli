SOURCES := $(wildcard src/kotlin/*/*.kt) $(wildcard src/kotlin/*/*/*.kt)
OUTPUT_DIR := build/
OTLIC := ../otlic.sh

.PHONY: otli-stdlib all clean

all: otli-stdlib

otli-stdlib: build/otli-stdlib.klib

clean:
	rm -rf build

build/otli-stdlib.klib: $(SOURCES)
	$(OTLIC) -Xallow-kotlin-package -Xmultifile-parts-inherit -verbose -output-klib -ir-output-dir build/ -ir-output-name otli-stdlib -Xstdlib-compilation -Xdont-warn-on-error-suppression $^
