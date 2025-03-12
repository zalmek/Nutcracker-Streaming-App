import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlinserialization)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.nutcrackerstreamingapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nutcrackerstreamingapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/d.tolstolutskiy/nutcracker")
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("keystore_password").toString()
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("key_alias").toString()
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("key_password").toString()

        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true
            proguardFiles(
                // Includes the default ProGuard rules files that are packaged with
                // the Android Gradle plugin. To learn more, go to the section about
                // R8 configuration files.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                // Includes a local, custom Proguard rules file
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirsts += setOf("**/*.so")
        }
    }
}

dependencies {
    implementation(libs.accompanist.permissions)
    // Camera plugin
    implementation (libs.camera.core)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.view)
    implementation (libs.androidx.camera.lifecycle)
    implementation(libs.qr.scanner.plugin)
    implementation(libs.androidx.ui.text.google.fonts)

    // Streaming
    implementation (libs.streampack)
    // For RTMP
    implementation (libs.streampack.extension.rtmp)
    implementation(libs.kotlinx.collections.immutable)
    // For SRT
    implementation (libs.streampack.extension.srt)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.converter.gson)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
