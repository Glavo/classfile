# Java Class-File API

[![Gradle Check](https://github.com/Glavo/classfile/actions/workflows/check.yml/badge.svg)](https://github.com/Glavo/classfile/actions/workflows/check.yml)
[![codecov](https://codecov.io/gh/Glavo/classfile/branch/main/graph/badge.svg?token=O9EUO58YKZ)](https://codecov.io/gh/Glavo/classfile)
[![Latest release](https://img.shields.io/maven-central/v/org.glavo/classfile)](https://github.com/Glavo/classfile/releases/latest)
[![javadoc](https://javadoc.io/badge2/org.glavo/classfile/javadoc.svg)](https://javadoc.io/doc/org.glavo/classfile)

This is a modern Java classfile manipulation and analysis library.
This library is a modern replacement for [ASM](https://asm.ow2.io/), extracted from the latest implementation of JDK,
requires Java 17 as a minimum version.

## Adding to your build

Maven:
```xml
<dependency>
  <groupId>org.glavo</groupId>
  <artifactId>classfile</artifactId>
  <version>0.5.0</version>
</dependency>
```

Gradle:
```kotlin
implementation("org.glavo:classfile:0.5.0")
```

## Get started

* [Tutorial](https://javadoc.io/doc/org.glavo/classfile/latest/org.glavo.classfile/org/glavo/classfile/package-summary.html): The best way to learn how to use the Class-File API;
* [Javadoc](https://javadoc.io/doc/org.glavo/classfile): Documentation for the current release;
* [Examples](./src/examples/java): Some examples showing the usage of Class-File API;
* [JDK Enhancement Proposal](https://openjdk.org/jeps/466): The JEP for Class-File API.

## Note

This library is extracted from OpenJDK 23 and renamed package `jdk.internal.classfile` to `org.glavo.classfile`.

In order to be compatible with Java 17, this library also copies some new APIs in Java 20/21:

* `java.lang.reflect.AccessFlag` -> `org.glavo.classfile.AccessFlag`
* `java.lang.reflect.ClassFileFormatVersion` -> `org.glavo.classfile.ClassFileFormatVersion`
* `java.lang.constant.ModuleDesc` -> `org.glavo.classfile.constant.ModuleDesc`
* `java.lang.constant.PackageDesc` -> `org.glavo.classfile.constant.PackageDesc`

Since the Class-File API is still in preview, it is not yet stable

[Here](CHANGELOG.md) is the change log.  

If you encounter problems when using this library, please feed back through [issue](https://github.com/Glavo/classfile/issues).

If you want to discuss the design of the Classfile API, please go to the [Classfile API mailing list](https://mail.openjdk.org/mailman/listinfo/classfile-api-dev) (classfile-api-dev@openjdk.org) for discussion.
