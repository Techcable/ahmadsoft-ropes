ahmadsoft-ropes ![GPLv3 license](https://img.shields.io/badge/license-GPLv3-brightgreen/) [![javadoc](https://img.shields.io/badge/javadoc-latest-informational)][javadoc-latest]
===============
The [Ropes 4 Java](http://ahmadsoft.org/ropes/index.html) package, exported to a git repo.

I use [JitPack](https://jitpack.io) for publishing both releases and javadocs.

Here is a link to the [javadocs for the latest release][javadoc-latest]. (_NOTE_: It is normal to take a while to load the docs, since JitPack may not have built it yet.)

<!-- Latest Javadoc using jitpack -->
[javadoc-latest]: https://jitpack.io/com/github/Techcable/ahmadsoft-ropes/latest/javadoc/

### Adding dependency to Gradle
First you need to add the jitpack maven repo (`https://jitpack.io`) to your gradle file `build.gradle.kts` if not already present:
```kotlin
// See Gradle docs for maven repositories: https://docs.gradle.org/8.1/userguide/declaring_repositories.html
repositories {
    // Add maven central
    mavenCentral()
    // Add jitpack
    maven {
        url = uri("https://jitpack.io")
    }
}
```

Then you can add the dependency later on (substituting whatever version is appropriate).
```kotlin
dependencies {
    implementation("com.github.Techcable:ahmadsoft-ropes:1.2.7")
}
```

## Original Code and History
This code is not my own. I just updated it to modern Java. It was originally created by Mr. Amin Ahmad. I found it [on his website](http://ahmadsoft.org/ropes/index.html).

I could not find the source repository on the original website, so I am simply turning each
source tarball from the [releases page](http://ahmadsoft.org/ropes/release.html)
into its own git commit.

See also the [original javadocs](http://ahmadsoft.org/ropes/doc/index.html)
