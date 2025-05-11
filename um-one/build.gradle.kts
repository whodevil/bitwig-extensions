import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("bitwig-extensions.conventions")
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

dependencies {
    implementation(project(":common"))
}