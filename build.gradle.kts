import de.undercouch.gradle.tasks.download.Download

plugins {
    `java-library`
    `maven-publish`
    id("de.undercouch.download").version("5.4.0")
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

run {
    val performanceBookDir = File(buildDir, "performance-books")

    data class TestBook(
        val id: String,
        val fileName: String,
        val url: String,
    ) {
        lateinit var task: Task

        val outputFile: File
            get() = File(performanceBookDir, this.fileName)
    }

    val performanceBooks = listOf(
        TestBook(
            id = "ChristmasCarol",
            fileName = "AChristmasCarol_CharlesDickens.txt",
            url = "https://gutenberg.org/files/24022/24022-8.txt"
        ),
        TestBook(
            id = "BenjaminFranklinBiography",
            fileName = "AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt",
            url = "https://gutenberg.org/cache/epub/20203/pg20203.txt"
        )
    )
    // Download the books used for the performance tests
    for (book in performanceBooks) {
        book.task = tasks.create<Download>("download${book.id}") {
            src(book.url)
            dest(book.outputFile)
            overwrite(false) // only ever needs to donload once

            outputs.file(book.outputFile)
        }
    }

    tasks.create("downloadPerformanceTestBooks") {
        dependsOn(*performanceBooks.map(TestBook::task).toTypedArray())
        outputs.files(*performanceBooks.map(TestBook::outputFile).toTypedArray())
    }
}

tasks.register<JavaExec>("runPerformanceTest") {
    dependsOn(tasks.findByName("downloadPerformanceTestBooks"))
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
