# Overview

#### Pocketbase Kotlin is a multiplatform Kotlin SDK for [Pocketbase](https://pocketbase.io) designed to be used both client and server side.

## Support

Pocketbase Kotlin offers support for [Pocketbase 0.20](https://github.com/pocketbase/pocketbase/releases/tag/v0.20.0) and above.
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
| Android                           |

*Want a platform supported? Open an [issue](https://github.com/agrevster/pocketbase-kotlin/issues), and we will try our
best to add it.*

### Installing

**Using this library requires the**
[KotlinX Serialization plugin](https://github.com/Kotlin/kotlinx.serialization#using-the-plugins-block)

To use Pocketbase Kotlin just add the following into your buildscript:
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.agrevster:pocketbase-kotlin:2.6.3")
}
```              