import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")

}

android {
    namespace = "com.vanstone.redsysa90prokeypos"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.vanstone.redsysa90prokeypos"
        minSdk = 29
        targetSdk = 33
        versionCode = 5
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        register("release") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
            storeFile = file("E:/SVN/Customers/Kron/Uganda_Apay/apk signature/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    android.applicationVariants.all {
        val variant = this
        val buildType = this.buildType.name
        val date = SimpleDateFormat("yyMMddHHmmss").format(Date())

        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "Redsys-A90Pro-MasterPOS_v${android.defaultConfig.versionName}_${android.defaultConfig.versionCode}_${date}_${buildType}.apk"
                output.outputFileName = outputFileName
            }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("libs\\CH34xUARTDriver.jar"))
    implementation(project(mapOf("path" to ":msgdialog")))
    implementation(files("libs\\AppSdkAidl_buildBy_20240418.jar"))
    implementation(files("libs\\vanstoneSdkClient-noemv_20240418.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("me.jahnen.libaums:core:0.10.0")

}