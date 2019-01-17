#!/bin/bash
pushd `dirname $0` > /dev/null
SCRIPT_PATH=`pwd`
popd > /dev/null

java -server -cp "${SCRIPT_PATH}/build/classes":"${SCRIPT_PATH}/build/som.jar" som.vm.Universe "$@"
