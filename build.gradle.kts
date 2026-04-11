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
        // TODO figure out how to do the right thing here and rename the license files so that they are included in the extension
        exclude("**/LICENSE*")
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
    implementation("com.bitwig:extension-api:20")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.hid4java:hid4java:0.8.0")
}
