// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    /*extensions() {
        var compose_version = "1.2.1"
        var hilt_plugin_version = "2.43.2"
        var kotlin_version = "1.5.31"
    }*/
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }

    }
    dependencies {
        classpath (libs.google.services)
        classpath (libs.gradle)
        classpath (libs.kotlin.gradle.plugin)
        classpath (libs.androidx.navigation.safe.args.gradle.plugin)
        classpath (libs.kotlin.serialization)
        classpath (libs.hilt.android.gradle.plugin)

//        classpath 'com.google.dagger:hilt-android-gradle-plugin:2.33-beta'


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}