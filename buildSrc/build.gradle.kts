plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("de.undercouch:gradle-download-task:5.4.+")
}

kotlin {
    jvmToolchain(17)
}