buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(kotlin("gradle-plugin", version = "2.1.0"))
        classpath(libs.google.services)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
}
