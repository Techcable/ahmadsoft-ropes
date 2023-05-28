import de.undercouch.gradle.tasks.download.Download
import java.io.File

plugins {
    id("de.undercouch.download")
}


data class TestBook(
    val fileName: String,
    val url: String,
) {}

val performanceBooks = listOf(
    TestBook(
        fileName = "AChristmasCarol_CharlesDickens.txt",
        url = "https://gutenberg.org/files/24022/24022-8.txt"
    ),
    TestBook(
        fileName = "AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt",
        url = "https://gutenberg.org/cache/epub/20203/pg20203.txt"
    )
)

tasks.create<Download>("downloadTestPerformanceBooks") {
    performanceBooks.forEach { book -> src(book.url)}
    dest(File(buildDir, "performance-books"))
    overwrite(false) // Don't care about up to date
    eachFile {
        val book = performanceBooks.find { it.url == sourceURL.toString() }!!
        relativePath = RelativePath(true, book.fileName)
    }
}

val TaskContainer.downloadTestPerformanceBooks: Download
    get() = getByName<Download>("downloadTestPerformanceBooks")