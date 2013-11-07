#!/usr/bin/env make -f

all:

clean:
	ant clean

compile: build/som.jar

build/som.jar:
	ant jar

test:
	ant test
