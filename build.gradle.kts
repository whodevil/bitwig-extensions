plugins {
    kotlin("jvm") version "2.0.21"
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
    modularity.inferModulePath.set(true)
}

tasks {
    named<Jar>("jar") {
        exclude("**/LICENSE.txt")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    register<Copy>("install") {
        group = "build"
        dependsOn("jar")
        from(getTasksByName("jar", false))
        val destDir = System.getenv("BITWIG_EXTENSIONS_LOCATION")
        val destDirFile = File(destDir)
        println("BITWIG_EXTENSIONS_LOCATION $destDir ${destDirFile.isDirectory()}")
        into(destDir)
        rename {"${project.name}.bwextension"}
    }
}

dependencies {
    api("com.bitwig:extension-api:20")
    api("com.google.inject:guice:7.0.0")
}
