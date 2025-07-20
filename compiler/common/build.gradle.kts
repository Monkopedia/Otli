plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksrpc)
}

kotlin {
    linuxX64()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ksrpc)
            }
        }
    }
}
