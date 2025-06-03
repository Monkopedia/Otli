plugins {
    alias(libs.plugins.kotlin)
}

kotlin {
}

dependencies {
    implementation(libs.kotlin.compiler)
    testImplementation(kotlin("test"))
}
