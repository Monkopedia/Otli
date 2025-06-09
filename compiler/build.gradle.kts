plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

kotlin {
    jvmToolchain(11)
}

tasks.jar {
    manifest.attributes["Main-Class"] = "com.monkopedia.otli.OtliCompiler"
}

dependencies {
    implementation(libs.kotlin.compiler)
    implementation(libs.kotlinx.serialization)
    testImplementation(kotlin("test"))
}
