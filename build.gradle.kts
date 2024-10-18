import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    val kotlinVersion = "2.0.20"
    val dokkaVersion = "1.9.20"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version dokkaVersion
    id("maven-publish")
    id("signing")

}

val ktorVersion = "3.0.0"
val kotlinSerializationVersion = "1.7.3"
val kotlinCoroutinesVersion = "1.9.0"
val kotlinTimeVersion = "0.6.1"


repositories {
    mavenCentral()
}


kotlin {
    explicitApi()

    jvmToolchain(18)

    jvm {
        withJava()
    }

    // Linux
    linuxX64()

    // MacOS
    macosX64()
    macosArm64()

    // iOS
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    // Windows
    mingwX64()

    sourceSets {
        commonMain {
            dependencies {
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinTimeVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        commonTest {
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

        val cioMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val cioTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        val winHTTPMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api("io.ktor:ktor-client-winhttp:$ktorVersion")
            }
        }

        val winHTTPTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
            }
        }

        fun KotlinSourceSet.configureDependencies(
            test: Boolean = false,
            winHTTP: Boolean = false,
        ) {
            if (winHTTP) {
                if (test) {
                    this.dependsOn(winHTTPTest)
                } else this.dependsOn(winHTTPMain)
            } else if (test) {
                this.dependsOn(cioTest)
            } else this.dependsOn(cioMain)
        }

        getByName("jvmMain").configureDependencies()
        getByName("jvmTest").configureDependencies(test = true)

        getByName("linuxX64Main").configureDependencies()
        getByName("linuxX64Test").configureDependencies(test = true)

        getByName("macosX64Main").configureDependencies()
        getByName("macosX64Test").configureDependencies(test = true)

        getByName("macosArm64Main").configureDependencies()
        getByName("macosArm64Test").configureDependencies(test = true)

        getByName("iosArm64Main").configureDependencies()
        getByName("iosArm64Test").configureDependencies(test = true)

        getByName("iosSimulatorArm64Main").configureDependencies()
        getByName("iosSimulatorArm64Test").configureDependencies(test = true)

        getByName("iosX64Main").configureDependencies()
        getByName("iosX64Test").configureDependencies(test = true)

        getByName("macosArm64Main").configureDependencies()
        getByName("macosArm64Test").configureDependencies(test = true)

        getByName("mingwX64Main").configureDependencies(winHTTP = true)
        getByName("mingwX64Test").configureDependencies(winHTTP = true, test = true)
    }
}

apply(from = "publish.gradle")