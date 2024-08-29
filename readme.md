# Pocketbase Kotlin
> Pocketbase Kotlin is a multi-platform Kotlin SDK for [Pocketbase](https://pocketbase.io).
> Current supported Pocketbase Versions *0.20+*, 
---

### More info can be found in [the docs](https://agrevster.github.io/pocketbase-kotlin/)

## Installation

**Using this library requires the**
[KotlinX Serialization plugin](https://github.com/Kotlin/kotlinx.serialization#using-the-plugins-block)

To use Pocketbase Kotlin just add the following into your buildscript:
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.agrevster:pocketbase-kotlin:2.6.2")
}
```