plugins {
    `java-library`
}

group = "org.ahmadsoft"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // Used for performance tests
    testImplementation("org.javolution:javolution:5.3.1")
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

