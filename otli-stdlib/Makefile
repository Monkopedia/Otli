SOURCES := $(wildcard src/kotlin/*/*.kt) $(wildcard src/kotlin/*/*/*.kt)
OUTPUT_DIR := build/
OTLIC := ../otlic.sh

TEST_SOURCES := $(wildcard src/test/*/*.kt) $(wildcard src/test/*/*/*.kt)
TEST_OUTPUT_DIR := build/test
TEST_EXE := build/test/test.otli

.PHONY: otli-stdlib all clean otli-test-source test

all: otli-stdlib

otli-stdlib: build/otli-stdlib.klib

clean:
	rm -rf build

build/otli-stdlib.klib: $(SOURCES)
	$(OTLIC) -Xallow-kotlin-package -output-klib -ir-output-dir $(OUTPUT_DIR) -ir-output-name otli-stdlib -Xstdlib-compilation -Xdont-warn-on-error-suppression $^

otli-test-source: $(TEST_SOURCES)
	mkdir -p $(TEST_OUTPUT_DIR)
	$(OTLIC) -Xallow-kotlin-package -ir-output-dir $(TEST_OUTPUT_DIR) -ir-output-name otli-stdlib-test -libraries=build/otli-stdlib.klib:../otli-test/build/otli-test.klib -Xdont-warn-on-error-suppression $^

$(TEST_EXE): otli-test-source $(wildcard $(TEST_OUTPUT_DIR)/*.c)
	clang -lcunit -I src/otli/include -o $(TEST_EXE) $(TEST_OUTPUT_DIR)/*.c

test: $(TEST_EXE)
	./$(TEST_EXE)

