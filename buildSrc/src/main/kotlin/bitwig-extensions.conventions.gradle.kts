plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks {
    named<Jar>("jar") {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    register<Copy>("install") {
        group = "build"
        dependsOn("jar")
        from(tasks.getByName("jar"))
        into(System.getenv("BITWIG_EXTENSIONS_LOCATION"))
        rename {"${project.name}.bwextension"}
    }
}
