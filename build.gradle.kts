// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // I only added this part indicating to gradle to go to mavenCentral to fetch plugins
    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlinserialization) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
}
