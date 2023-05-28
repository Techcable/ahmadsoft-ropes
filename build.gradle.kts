
plugins {
    `java-library`
    `maven-publish`
    `download-test-books`
}

group = "org.ahmadsoft"
version = "1.3.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
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

tasks.register<JavaExec>("runPerformanceTest") {
    dependsOn(tasks.downloadTestPerformanceBooks.get())
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("org.ahmadsoft.ropes.test.PerformanceTest")
    jvmArgs("--add-opens", "org.ahmadsoft.ropes/org.ahmadsoft.ropes.impl=ALL-UNNAMED")
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
