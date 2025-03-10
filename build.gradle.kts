import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

enum class HttpClientType {
    WIN_HTTP, CIO;
}

kotlin {
    explicitApi()

    androidTarget {
        publishAllLibraryVariants()
    }

    jvmToolchain(18)
    jvm()

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

    // Android
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()


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
            httpClientType: HttpClientType = HttpClientType.CIO,
        ) {
            if (httpClientType == HttpClientType.WIN_HTTP) {
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

        getByName("mingwX64Main").configureDependencies(httpClientType = HttpClientType.WIN_HTTP)
        getByName("mingwX64Test").configureDependencies(test=true, httpClientType = HttpClientType.WIN_HTTP)


        getByName("androidMain").configureDependencies()
        getByName("androidUnitTest").configureDependencies(test = true)

        getByName("androidNativeArm32Main").configureDependencies()
        getByName("androidNativeArm32Test").configureDependencies(test = true)

        getByName("androidNativeArm64Main").configureDependencies()
        getByName("androidNativeArm64Test").configureDependencies(test = true)

        getByName("androidNativeX64Main").configureDependencies()
        getByName("androidNativeX64Test").configureDependencies(test = true)

        getByName("androidNativeX86Main").configureDependencies()
        getByName("androidNativeX86Test").configureDependencies(test = true)
    }
}

android {
    namespace = "io.github.agrevster.pocketbaseKotlin"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
}

apply(from = "publish.gradle")