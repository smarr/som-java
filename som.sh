#!/bin/sh
java -server -cp build/som.jar som.vm.Universe \
		"$@"
