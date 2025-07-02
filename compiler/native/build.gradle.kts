plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ksrpc)
}

kotlin {
    linuxX64 {
        binaries {
            sharedLib {
                this.export(libs.ksrpc.jni)
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":common"))
                api(libs.ksrpc.jni)
            }
        }
    }
}
