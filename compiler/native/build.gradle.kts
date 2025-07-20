plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksrpc)
}

val cflags = arrayOf<String>("-I/usr/include")

val ldflags = emptyArray<String>()

kotlin {
    linuxX64 {
        binaries {
            sharedLib {
                this.export(libs.ksrpc.jni)
            }
        }
        compilations["main"].cinterops {
            this.create("libclang") {
                definitionFile = file("clang.def")
                this.compilerOpts += cflags
                this.linkerOpts += ldflags
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
