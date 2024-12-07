import org.jetbrains.kotlin.gradle.utils.IMPLEMENTATION

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
//    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
//    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false

}
/*

dependencies {
    IMPLEMENTATION("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}*/
