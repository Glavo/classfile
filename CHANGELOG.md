# Change Log

## Version 0.4.0 (In development)

Sync upstream changes:

* [JDK-8304164](https://github.com/openjdk/jdk/commit/b2639e1d6246a7e1aab1d9d15add7979adf40766)

Now the Classfile API is based on openjdk/jdk@b2639e1.

## Version 0.3.0 (2023-03-17)

Sync upstream changes:

* [JDK-8303910](https://github.com/openjdk/jdk/commit/43eca1dcb197e3615b6077a5d8aef28f32a7724c)
* [JDK-8294962](https://github.com/openjdk/jdk/commit/714b5f036fc70d8d1d4d3ec8777fe95cffc0fe5b)
* [JDK-8304161](https://github.com/openjdk/jdk/commit/7dbab81d3c06efb1225c4d57ad3eb4960fcf5cc6)

Now the Classfile API is based on [openjdk/jdk@7dbab81](https://github.com/openjdk/jdk/commit/7dbab81d3c06efb1225c4d57ad3eb4960fcf5cc6).

Important changes:

* Dropping the packages parameter from `Classfile.buildModule` and `Classfile.buildModuleTo`
* New API: `TypeKind.from(TypeDescriptor.OfField<?>)`

## Version 0.2.0 (2023-03-14)

* Make `Classfile.build` generate Java 17 bytecode instead of Java 21 by default

## Version 0.1.0 (2023-03-13)

Initial version, based on [openjdk/jdk@4655b79](https://github.com/openjdk/jdk/commit/4655b790d0b39b4ddabde78d7b3eed196b1152ed).