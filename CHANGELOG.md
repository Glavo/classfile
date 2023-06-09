# Changelog

## Version 0.5.0 (In development)

Sync upstream changes:

* [JDK-8304837](https://github.com/openjdk/jdk/commit/a2d3fc83b0dd7eea38e1dd5898a97d6d7ff60194)
* [JDK-8304937](https://github.com/openjdk/jdk/commit/dc4096ce136c867e0806070a2d7c8b4efef5294c)
* [JDK-8304031](https://github.com/openjdk/jdk/commit/cd5d0ff5b29065222ffafbc4fb04b90f6f8909e2)
* [JDK-8305990](https://github.com/openjdk/jdk/commit/a05560d99352bd5952f3feef37b56dceb74ede3b)

## Version 0.4.0 (2023-03-23)

Sync upstream changes:

* [JDK-8304164](https://github.com/openjdk/jdk/commit/b2639e1d6246a7e1aab1d9d15add7979adf40766)
* [JDK-8304502](https://github.com/openjdk/jdk/commit/0156909ab38072869e2eb9f5049042b9199d14a0)

Now the Classfile API is based on [openjdk/jdk@0156909](https://github.com/openjdk/jdk/commit/0156909ab38072869e2eb9f5049042b9199d14a0).

Important changes:

* Throws `IllegalArgumentException` when the class is not resolved

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