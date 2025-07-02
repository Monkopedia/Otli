plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ksrpc)
}

kotlin {
    jvmToolchain(11)
    sourceSets {
        main {
            resources.srcDir(projectDir.resolve("build/generated/lib/resources"))
        }
    }
}

tasks.jar {
    manifest.attributes["Main-Class"] = "com.monkopedia.otli.OtliCompiler"
}
val copyLib = tasks.register("copyLib", Copy::class) {
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val project = rootProject.findProject(":native") ?: error("Missing native project")
    val buildDir = project.projectDir.resolve("build")
    val tasks = project.tasks
    when (hostOs) {
        "Mac OS X" -> {
            if (arch == "aarch64") {
                dependsOn(tasks.findByName("linkReleaseSharedMacosArm64"))
                from(buildDir.resolve("bin/macosArm64/releaseShared/libnative.dylib"))
                destinationDir = projectDir.resolve("build/generated/lib/resources/libs/")
            } else {
                dependsOn(tasks.findByName("linkReleaseSharedMacosX64"))
                from(buildDir.resolve("bin/macosX64/releaseShared/libnative.dylib"))
                destinationDir = projectDir.resolve("build/generated/lib/resources/libs/")
            }
        }
        "Linux" -> {
            dependsOn(tasks.findByName("linkDebugSharedLinuxX64"))
            from(buildDir.resolve("bin/linuxX64/debugShared/libnative.so"))
            destinationDir = projectDir.resolve("build/generated/lib/resources/libs/")
        }
        else -> throw GradleException(
            "Host OS '$hostOs' is not supported in Kotlin/Native $project."
        )
    }
    doFirst {
    }
}

afterEvaluate {
    tasks["processResources"].dependsOn(copyLib)
}

dependencies {
    implementation(libs.kotlin.compiler)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ksrpc)
    implementation(libs.ksrpc.jni)
    implementation(project(":common"))
    testImplementation(kotlin("test"))
}
