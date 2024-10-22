import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}


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
                api(libs.ktor.client.core)
                api(libs.ktor.client.content.negociation)
                api(libs.ktor.serialization.json)

                api(libs.kotlin.coroutines)
                api(libs.kotlin.datetime)
                api(libs.kotlin.serlization.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negociation)
                implementation(libs.ktor.serialization.json)

                implementation(libs.kotlin.coroutines)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serlization.json)
            }
        }

        val cioMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.ktor.client.cio)
            }
        }

        val cioTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        val winHTTPMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.ktor.client.winhttp)
            }
        }

        val winHTTPTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.ktor.client.winhttp)
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