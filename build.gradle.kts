// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")

    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2") // ✅ Version required
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.27")
    }
}
