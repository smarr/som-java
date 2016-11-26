SOM - Simple Object Machine
===========================

SOM is a minimal Smalltalk dialect used to teach VM construction at the [Hasso
Plattner Institute][SOM]. It was originally built at the University of Århus
(Denmark) where it was also used for teaching.

Currently, implementations exist for Java (SOM), C (CSOM), C++ (SOM++), and
Squeak/Pharo Smalltalk (AweSOM).

A simple SOM Hello World looks like:

```Smalltalk
Hello = (
  run = (
    'Hello World!' println.
  )
)
```

This repository contains a plain Java implementation of SOM, including an implementation of the SOM standard library. Please see the [main project page][SOMst] for links to the VM implementation.

Make sure to initialize the shared Smalltalk standard library, tests, and examples by importing the git submodule:

    $ git submodule update --init

To build and run SOM, Java 8 or newer is required.

SOM can be built with Ant:

    $ ant jar

Afterwards, the tests can be executed with:

    $ ./som.sh -cp Smalltalk TestSuite/TestHarness.som

or with:

    $ ant test
 
A simple Hello World program is executed with:

    $ ./som.sh -cp Smalltalk Examples/Hello/Hello.som

Information on previous authors are included in the AUTHORS file. This code is
distributed under the MIT License. Please see the LICENSE file for details.

Build Status
------------

Thanks to Travis CI, all commits of this repository are tested.
The current build status is: [![Build Status](https://travis-ci.org/SOM-st/som-java.png?branch=master)](https://travis-ci.org/SOM-st/som-java)

 [SOM]: http://www.hpi.uni-potsdam.de/hirschfeld/projects/som/
 [SOMst]: https://travis-ci.org/SOM-st/
