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
