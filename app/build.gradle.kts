import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

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

        versionName = System.getenv("VERSION") ?: "LOCAL"
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
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
            applicationIdSuffix = ".debug"
        }
        named("release").configure {
            isDebuggable = false
            isJniDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("beta") {
            dimension = "version"
            versionNameSuffix = "Beta"
        }
        create("full") {
            dimension = "version"
        }
    }

    kapt {
        correctErrorTypes = true
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

    implementation ("com.github.fondesa:kpermissions:3.4.0")
    implementation ("com.github.fondesa:kpermissions-coroutines:3.4.0")

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(androidx.camera.camera2)
    implementation(androidx.camera.lifecycle)
    implementation(androidx.camera.view)

    implementation("androidx.compose.runtime:runtime-tracing:1.0.0-alpha01")
    implementation("androidx.tracing:tracing-ktx:1.2.0-alpha01")

    implementation("io.realm.kotlin:library-base:1.5.2")

    implementation("io.sentry:sentry-android:6.12.1")

    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    implementation("io.coil-kt:coil-compose:1.3.2")
    implementation("io.coil-kt:coil-gif:1.4.0")

    implementation(androidx.compose.ui.tooling.preview)
    implementation(androidx.lifecycle.runtime)

    implementation("com.google.code.gson:gson:2.10.1")

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
    //Compose
    implementation(androidx.compose.ui)
    implementation(androidx.compose.material)
    implementation(androidx.compose.ui.tooling)
    implementation(androidx.activity.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(androidx.compose.runtime.livedata)
    //implementation(androidx.compose.foundation.layout)

    //AndroidX
    //implementation(libs.material)
    implementation(androidx.appcompat)
    implementation(androidx.recyclerview)
    implementation(androidx.constraintlayout)
    implementation(androidx.annotation)
    implementation(androidx.navigation.fragment)
    implementation(androidx.navigation.compose)
    implementation(androidx.navigation.ui)
    implementation(androidx.fragment)

    //END AndroidX

    //Firebase
    api(platform(libs.firebase.bom))
    api("com.google.firebase:firebase-analytics-ktx")
    api("com.google.firebase:firebase-auth-ktx")
    api("com.google.firebase:firebase-firestore-ktx")
    api("com.google.firebase:firebase-functions-ktx")
    api("com.google.firebase:firebase-messaging-ktx")
    api("com.google.firebase:firebase-storage-ktx")
    api("com.google.firebase:firebase-crashlytics-ktx")
    api("com.google.firebase:firebase-perf-ktx")
    api("com.google.firebase:firebase-database-ktx")
    api("com.google.firebase:firebase-config-ktx")
    api("com.google.firebase:firebase-inappmessaging-display-ktx")
    api("com.google.firebase:firebase-dynamic-links-ktx")
    api("com.google.mlkit:text-recognition:16.0.0-beta6")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    api("com.firebaseui:firebase-ui-auth:8.0.2")
    //END Firebase

    //Drawer
    implementation("com.mikepenz:materialdrawer-nav:8.4.5")
    implementation("com.mikepenz:materialdrawer:8.4.5")
    implementation("com.mikepenz:materialdrawer-iconics:8.4.5")
    //implementation("com.mikepenz:google-material-typeface:4.0.0.1-kotlin@aar")


    //END Drawer

    //Dialogs
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:color:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    //END Dialogs

    api ("com.github.bumptech.glide:glide:4.14.2") {
        exclude(group = "com.android.support")
    }


    implementation("com.github.skydoves:only:1.0.8")
    implementation(androidx.lifecycle.livedata)

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.airbnb.android:lottie-compose:5.2.0")

    api("com.google.accompanist:accompanist-glide:0.15.0")
    api("com.google.accompanist:accompanist-insets:0.28.0")
    api(libs.accompanist.pager)
    api(libs.accompanist.pager.indicators)
    api(libs.accompanist.navigation.animation)
    api(libs.accompanist.flowlayout)
    api(libs.accompanist.placeholder.material)
    api(libs.accompanist.swiperefresh)

    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")

    implementation("com.github.stfalcon-studio:StfalconImageViewer:v1.0.1")

    implementation("com.google.android.gms:play-services-maps:18.1.0")

    implementation("com.google.maps.android:maps-ktx:3.4.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0")
    implementation("com.github.jeziellago:compose-markdown:0.2.6")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(androidx.compose.material.icons.extended)

    implementation("io.qonversion.android.sdk:sdk:4.+")
    implementation("com.airbnb.android:mavericks:3.0.1")
    implementation("com.airbnb.android:mavericks-compose:3.0.1")
    implementation("com.airbnb.android:mavericks-hilt:3.0.1")
    implementation(androidx.core.splashscreen)
    implementation(androidx.compose.ui.text.googleFonts)
    implementation(androidx.compose.animation.graphics)


    implementation(androidx.compose.material3)

    // screen modules
    implementation("com.github.MFlisar.MaterialPreferences:screen:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-bool:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-input:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-choice:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-color:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-slider:1.1.2")
    implementation("com.github.MFlisar.MaterialPreferences:screen-image:1.1.2")

    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:info:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:color:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:duration:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:date-time:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:option:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:list:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:input:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:emoji:1.0.3")
    implementation("com.maxkeppeler.sheets-compose-dialogs:state:1.0.3")

}