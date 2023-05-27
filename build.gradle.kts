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
    testImplementation("junit:junit:4.13.2")

    // Used for performance tests
    testImplementation("org.javolution:javolution:5.3.1")
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

tasks.compileJava {
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
