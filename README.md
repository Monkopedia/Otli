# Otli

Otli is my current answer to embedded development. I know C/C++ and the ins/outs of it, and have no
problem with coding with it in theory. However, I have become old and set in my ways, and 99.99% of
all my other programming is in kotlin. So the problem is not that I don't want to program in C, its
that I constantly use kotlin syntax and features and am sad when they are not available. Otli is an
attempt to see if I can make a subset of the Kotlin language syntax be cross-compileable into C
code.

Will this work? we'll see. Probably not.

## Structure plans

My current thinking is it would look something like this:
 - A preprocessor that throws errors if you use features of kotlin not supported by otli
 - A custom invocation of the compiler to compile to IR, but no further (if this is possible)
 - A compiler plugin to pick up from the IR and try to generate C

Presumably if all of that works, then maybe build some sort of header parser that can generate fake
kotlin targets to depend on for fake symbols for std-c libs and allow generation from headers for
interop.

## Goals

Well obviously the first goal would be to be able to have any code convert. So first milestone is
probably declare a variable, do a math, and print the result, and be able to compile that into
valid C code.

Milestone 1:
 - Declare vars
 - Do intrinsic operations
 - Call printf

After this, things are not totally clear yet, but here are some thoughts:

Milestone 2:
 - Functions
 - Arrays
 - Multiple files??

Milestone 3:
 - data classes as structs
 - extension methods
 - Lists?

## Complications

There are a large number of complications, but top of mind at the moment is how to handle pointers
and memory management. The naive approach would be to pass pointers around, only return intrinsic
types, and over-allocate things. However, I'm sure that'll fall apart very quickly.
