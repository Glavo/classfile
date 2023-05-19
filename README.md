# Java Classfile API

[![Gradle Check](https://github.com/Glavo/classfile/actions/workflows/check.yml/badge.svg)](https://github.com/Glavo/classfile/actions/workflows/check.yml)
[![codecov](https://codecov.io/gh/Glavo/classfile/branch/main/graph/badge.svg?token=O9EUO58YKZ)](https://codecov.io/gh/Glavo/classfile)
[![Latest release](https://img.shields.io/maven-central/v/org.glavo/classfile)](https://github.com/Glavo/classfile/releases/latest)
[![javadoc](https://javadoc.io/badge2/org.glavo/classfile/javadoc.svg)](https://javadoc.io/doc/org.glavo/classfile)

This is a modern Java classfile manipulation and analysis library.
This library is a modern replacement for [ASM](https://asm.ow2.io/), extracted from the latest internal implementation of JDK,
requires Java 17 as a minimum version.

## Adding to your build

Please replace `${version}` with the [current version](https://github.com/Glavo/classfile/releases) of this library.

Maven:
```xml
<dependency>
  <groupId>org.glavo</groupId>
  <artifactId>classfile</artifactId>
  <version>${version}</version>
</dependency>
```

Gradle:
```kotlin
implementation("org.glavo:classfile:${version}")
```

## Get started

The core class of the Classfile API is [`Classfile`](src/main/java/org/glavo/classfile/Classfile.java).

A tutorial is in the works, now you can look at the [examples](src/examples/java), or check out the [JEP draft](https://openjdk.org/jeps/8280389) for help.

## Note

This library is extracted from [OpenJDK 21](https://github.com/openjdk/jdk/commit/4655b790d0b39b4ddabde78d7b3eed196b1152ed)
and renamed package `jdk.internal.classfile` to `org.glavo.classfile`.

In order to be compatible with Java 17, this library also copies some new APIs in Java 20/21:

* `java.lang.reflect.AccessFlag` -> `org.glavo.classfile.AccessFlag`
* `java.lang.reflect.ClassFileFormatVersion` -> `org.glavo.classfile.ClassFileFormatVersion`
* `java.lang.constant.ModuleDesc` -> `org.glavo.classfile.constant.ModuleDesc`
* `java.lang.constant.PackageDesc` -> `org.glavo.classfile.constant.PackageDesc`

Because the implementation in JDK is still in the `jdk.internal.classfile`
package and does not belong to the public API, the API of this library is still unstable
until the stable version of [JEP Draft 8280389](https://openjdk.org/jeps/8280389) is released.

[Here](CHANGELOG.md) is the change log.  

If you encounter problems when using this library, please feed back through [issue](https://github.com/Glavo/classfile/issues).

If you want to discuss the design of the Classfile API, please go to the [Classfile API mailing list](https://mail.openjdk.org/mailman/listinfo/classfile-api-dev) (classfile-api-dev@openjdk.org) for discussion.
