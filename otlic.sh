#!/bin/bash

## TODO: Anything remotely reasonable
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
java -jar $SCRIPT_DIR/compiler/build/libs/otlic-all.jar $@
