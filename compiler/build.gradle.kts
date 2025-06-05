plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
}

dependencies {
    implementation(libs.kotlin.compiler)
    implementation(libs.kotlinx.serialization)
    testImplementation(kotlin("test"))
}
