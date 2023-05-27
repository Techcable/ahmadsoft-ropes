plugins {
    `java-library`
    `maven-publish`
}

group = "org.ahmadsoft"
version = "1.3.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit)

    // Used for performance tests
    testImplementation(libs.javolution)
}

val javaVersion = 17

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        // NOTE: This is required for jitpack, which defaults to Java 8
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(javaVersion)
}

tasks.javadoc {
    (options as CoreJavadocOptions).addBooleanOption("Xdoclint:all,-missing", true)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
