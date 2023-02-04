import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.EnumSet
import io.sentry.android.gradle.extensions.InstrumentationFeature

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("io.realm.kotlin")
    id("com.apollographql.apollo3").version("3.7.4")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.firebase.firebase-perf")
    id("io.sentry.android.gradle").version("3.4.1")
}

apollo {
    service("service") {
        packageName.set("com.austinhodak.thehideout")
        codegenModels.set("responseBased")
    }
}

android {
    namespace = "com.austinhodak.thehideout"

    compileSdk = 33

    defaultConfig {
        applicationId = "com.austinhodak.thehideout"
        minSdk = 24
        targetSdk = 33

        versionName = System.getenv("VERSION") ?: gradleLocalProperties(rootDir).getProperty("versionName")
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?:
        gradleLocalProperties(rootDir).getProperty("versionCode")?.toIntOrNull() ?: 1
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../TheHideoutKeystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: gradleLocalProperties(rootDir).getProperty("storePassword")
            keyAlias = System.getenv("KEYSTORE_ALIAS") ?: gradleLocalProperties(rootDir).getProperty("keyAlias")
            keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: gradleLocalProperties(rootDir).getProperty("keyPassword")
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        named("debug").configure {
            //applicationIdSuffix = ".debug"
        }
        named("release").configure {
            isDebuggable = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("beta") {
            dimension = "version"
            versionNameSuffix = " Beta"
        }
        create("full") {
            dimension = "version"
        }
    }

    kapt {
        correctErrorTypes = true
    }
}

sentry {
    tracingInstrumentation {
        enabled.set(true)
        features.set(EnumSet.allOf(InstrumentationFeature::class.java) - InstrumentationFeature.DATABASE)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    val composeBom = platform(androidx.compose.bom)
    implementation(androidx.browser)
    implementation(project(":tarkovapi"))
    implementation(platform(androidx.compose.bom))
    implementation(platform(libs.kotlin.bom))

    implementation("com.google.firebase:firebase-appcheck-debug:16.1.1")

    api(libs.kotlinx.datetime)

    implementation(libs.kpermissions)
    implementation(libs.kpermissions.coroutines)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(androidx.bundles.camera)
    implementation(androidx.bundles.lifecycle)

    implementation(androidx.compose.runtime.tracing)
    implementation(androidx.tracing)

    implementation(libs.realm)

    implementation(libs.sentry)

    implementation(libs.oss.licenses)

    implementation(libs.bundles.coil)

    implementation(androidx.compose.ui.tooling.preview)


    implementation(libs.gson)

    // Tests
    testImplementation(libs.junit4)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.espresso.core)
    androidTestImplementation(androidx.compose.ui.test.junit4)

    // Core
    implementation(androidx.core)

    // Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(androidx.hilt.work)

    implementation(androidx.compose.ui)
    implementation(androidx.compose.material)
    implementation(androidx.compose.ui.tooling)
    implementation(androidx.activity.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(androidx.compose.runtime.livedata)



    implementation(androidx.appcompat)
    implementation(androidx.recyclerview)
    implementation(androidx.constraintlayout)
    implementation(androidx.annotation)
    implementation(androidx.navigation.fragment)
    implementation(androidx.navigation.compose)
    implementation(androidx.navigation.ui)
    implementation(androidx.fragment)


    api(platform(libs.firebase.bom))
    api(libs.bundles.firebase)
    implementation(libs.mlkit.text)

    implementation(libs.firebase.auth.ui)
    //Drawer

    implementation(libs.drawer)

    //END Drawer

    //Dialogs
    implementation(libs.bundles.afollestad.dialogs)
    //END Dialogs


    implementation(libs.skydoves.only)

    api(libs.timber)

    implementation(libs.lottie.compose)

    api("com.google.accompanist:accompanist-insets:0.28.0")
    api(libs.accompanist.pager)
    api(libs.accompanist.pager.indicators)
    api(libs.accompanist.navigation.animation)
    api(libs.accompanist.flowlayout)
    api(libs.accompanist.placeholder.material)
    api(libs.accompanist.swiperefresh)

    implementation(libs.bundles.google.play)

    implementation(libs.stfalcon.imageviewer)

    implementation(libs.bundles.maps)

    implementation(libs.compose.markdown)

    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    implementation(androidx.compose.material.icons.extended)

    implementation(libs.qonversion)
    implementation(libs.bundles.mavericks)
    implementation(androidx.core.splashscreen)
    implementation(androidx.compose.ui.text.googleFonts)
    implementation(androidx.compose.animation.graphics)


    implementation(androidx.compose.material3)

    // screen modules
    implementation(libs.bundles.preferences.screen)
    implementation(libs.bundles.compose.dialogs)
}