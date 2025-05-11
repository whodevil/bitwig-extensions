plugins {
    id("bitwig-extensions.conventions")
    kotlin("jvm")
    `java-library`
}

dependencies {
    api("com.bitwig:extension-api:20")
    api("com.google.inject:guice:4.1.0")
}
