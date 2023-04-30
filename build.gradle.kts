plugins {
    `java-library`
    `maven-publish`
}

group = "org.ahmadsoft"
version = "1.2.6-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // Used for performance tests
    testImplementation("org.javolution:javolution:5.3.1")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        // NOTE: This is required for jitpack, which defaults to Java 8
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.compileJava {
    // NOTE: This version is considered 'obselete'
    //
    // TODO: Upgrade to requiring Java 8 or higher
    options.release.set(7)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.ahmadsoft.ropes")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
