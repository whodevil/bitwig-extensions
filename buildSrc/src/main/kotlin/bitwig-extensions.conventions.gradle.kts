apply(plugin = "java")

tasks {

    register<Copy>("transit") {
        dependsOn("jar")
        from(tasks.getByName("jar"))
        from(configurations.getAt("runtimeClasspath"))
        into("${project.layout.buildDirectory.get()}/transit")
    }

    register<Zip>("package") {
        from(tasks.getByName("transit"))
        destinationDirectory = layout.buildDirectory.dir("archives")
    }

    register<Copy>("install") {
        from(tasks.getByName("package"))
        into(System.getenv("BITWIG_EXTENSIONS_LOCATION"))
        rename {"${project.name}.bwextension"}
    }
}
