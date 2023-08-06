# Overview

#### Pocketbase Kotlin is a multiplatform Kotlin SDK for [Pocketbase](https://pocketbase.io) designed to be used both client and server side.

## Support

Pocketbase Kotlin offers support for [Pocketbase 0.17](https://github.com/pocketbase/pocketbase/releases/tag/v0.16.10)
and above.
Support for new Pocketbase releases will be added as soon as possible.

Currently, the following platforms are supported,

| Supported Platforms               |       
|-----------------------------------|       
| JVM                               |       
| Linux (x64)                       |       
| Windows (x64)                     |       
| Mac OS (x64) (arm x64)            |       
| IOS (arm x64) (x64) (sim arm x64) |

*Want a platform supported? Open an [issue](https://github.com/agrevster/pocketbase-kotlin/issues), and we will try our
best to add it.*

### Installing

Just add the following into your buildscript:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.agrevster:pocketbase-kotlin:2.4.0")
}
```              