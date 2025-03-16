plugins {
    id("info.offthecob.Settings") version "1.0.17"
}

rootProject.name = "bitwig-extensions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.bitwig.com")
        }
    }

    versionCatalogs {
        create("libs") {
            from("info.offthecob.platform:catalog:1.0.17")
            library("offthecob-platform", "info.offthecob.platform:bom:1.0.17")
        }
    }
}