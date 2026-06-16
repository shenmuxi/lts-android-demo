// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

ext {
    set("compileSdkVersion", 33)
    set("buildToolsVersion", "33.0.1")
    set("compileSdk", 33)
    set("minSdk", 18)
    set("targetSdk", 33)
    set("sdk_version_code", 130)
    set("sdk_version_name", "1.0.30")
    set("PUBLISH_GROUP_ID", "io.github.lts-sdk")
    set("PUBLISH_ARTIFACT_ID", "lts-sdk-android")
    set("javaVersion", JavaVersion.VERSION_1_8)
    set("jvmTarget", "1.8")
}