plugins {
    kotlin("jvm") version "2.0.21"
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    named<Jar>("jar") {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    register<Copy>("install") {
        group = "build"
        dependsOn("jar")
        from(getTasksByName("jar", false))
        into(System.getenv("BITWIG_EXTENSIONS_LOCATION"))
        rename {"${project.name}.bwextension"}
    }
}

dependencies {
    api("com.bitwig:extension-api:20")
    api("com.google.inject:guice:4.1.0")
}
