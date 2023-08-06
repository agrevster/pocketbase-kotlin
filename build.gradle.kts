import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    val kotlinVersion = "1.8.22"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("org.jetbrains.dokka") version "1.8.20"
    id("maven-publish")
    id("signing")

}
val ktorVersion = "2.2.4"
val kotlinSerializationVersion = "1.5.1"
val kotlinCoroutinesVersion = "1.6.4"
val kotlinTimeVersion = "0.4.0"

val nativeCIOMainSets: MutableList<KotlinSourceSet> = mutableListOf()
val nativeCIOTestSets: MutableList<KotlinSourceSet> = mutableListOf()
val nativeWinHTTPMainSets: MutableList<KotlinSourceSet> = mutableListOf()
val nativeWinHTTPTestSets: MutableList<KotlinSourceSet> = mutableListOf()

val host: Host = getHostType()

repositories {
    mavenCentral()
}


kotlin {
    fun isPublishedBuild(): Boolean = System.getenv("PUBLISHING") == "true"

    fun addNativeTarget(preset: KotlinTargetPreset<*>, desiredHost: Host) {
        val target = targetFromPreset(preset)
        if (desiredHost == Host.WINDOWS) {
            nativeWinHTTPMainSets.add(target.compilations.getByName("main").kotlinSourceSets.first())
            nativeWinHTTPTestSets.add(target.compilations.getByName("test").kotlinSourceSets.first())
        } else {
            nativeCIOMainSets.add(target.compilations.getByName("main").kotlinSourceSets.first())
            nativeCIOTestSets.add(target.compilations.getByName("test").kotlinSourceSets.first())
        }
        if (host != desiredHost) {
            target.compilations.configureEach {
                compileKotlinTask.enabled = isPublishedBuild()
            }
        }
    }
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }


    // Linux
    addNativeTarget(presets["linuxX64"], Host.LINUX)

    // MacOS
    addNativeTarget(presets["macosX64"], Host.MAC_OS)
    addNativeTarget(presets["macosArm64"], Host.MAC_OS)

    // iOS
    addNativeTarget(presets["iosArm64"], Host.MAC_OS)
    addNativeTarget(presets["iosX64"], Host.MAC_OS)
    addNativeTarget(presets["iosSimulatorArm64"], Host.MAC_OS)

    // Windows
    addNativeTarget(presets["mingwX64"], Host.WINDOWS)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinTimeVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinTimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationVersion")
            }
        }

        val nativeCIOMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val nativeCIOTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val nativeWinHTTPMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
            }
        }

        val nativeWinHTTPTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
            }
        }

        val jvmMain by getting { dependsOn(nativeCIOMain) }
        val jvmTest by getting { dependsOn(nativeCIOTest) }

        configure(nativeCIOMainSets) { dependencies { dependsOn(nativeCIOMain) } }
        configure(nativeCIOTestSets) { dependencies { dependsOn(nativeCIOTest) } }

        configure(nativeWinHTTPMainSets) { dependencies { dependsOn(nativeWinHTTPMain) } }
        configure(nativeWinHTTPTestSets) { dependencies { dependsOn(nativeWinHTTPTest) } }
    }
}

fun getHostType(): Host {
    val hostOs = System.getProperty("os.name")
    return when {
        hostOs.startsWith("Windows") -> Host.WINDOWS
        hostOs.startsWith("Mac") -> Host.MAC_OS
        hostOs == "Linux" -> Host.LINUX
        else -> throw Error("Invalid host.")
    }
}

fun isPublicationAllowed(name: String): Boolean {
    println("name: $name")
    return when {
        name.startsWith("mingw") -> host == Host.WINDOWS
        name.startsWith("macos") ||
                name.startsWith("ios") ||
                name.startsWith("watchos") ||
                name.startsWith("tvos") -> host == Host.MAC_OS
        else -> host == Host.LINUX
    }
}
enum class Host { WINDOWS, MAC_OS, LINUX }

apply(from = "publish.gradle")