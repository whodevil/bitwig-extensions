rootProject.name = "bitwig-extensions"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.bitwig.com")
        }
    }
}

include("common", "mpd", "um-one", "pedalboard")