plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
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
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release_keystore.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEYSTORE_ALIAS") ?: ""
            keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: ""
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
    implementation(project(":tarkovapi"))
    implementation(platform(androidx.compose.bom))


    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("io.coil-kt:coil-gif:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    implementation("com.google.code.gson:gson:2.10")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Core
    implementation(androidx.core)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android-compiler:2.44.2")

    //Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation(libs.accompanist.systemuicontroller)
    implementation("androidx.compose.runtime:runtime-livedata")

    //AndroidX
    implementation(libs.material)
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
    //api("com.google.firebase:firebase-perf-ktx")
    api("com.google.firebase:firebase-database-ktx")
    api("com.google.firebase:firebase-config-ktx")
    api("com.google.firebase:firebase-inappmessaging-display-ktx")
    api("com.google.firebase:firebase-dynamic-links-ktx")
    //api("com.google.android.gms:play-services-ads")
    // api("com.google.android.gms:play-services-mlkit-text-recognition:17.0.1")
    api("com.google.mlkit:text-recognition:16.0.0-beta6")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-iid:21.1.0")

    api("com.firebaseui:firebase-ui-auth:8.0.2")
    //END Firebase

    //Drawer
    implementation("com.mikepenz:materialdrawer-nav:8.4.3")
    implementation("com.mikepenz:materialdrawer:8.4.3")
    implementation("com.mikepenz:materialdrawer-iconics:8.4.3")
    implementation("com.mikepenz:google-material-typeface:4.0.0.1-kotlin@aar")


    //END Drawer

    //Dialogs
    implementation("com.afollestad.material-dialogs:core:3.3.0")
    implementation("com.afollestad.material-dialogs:color:3.3.0")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    //END Dialogs

    api ("com.github.bumptech.glide:glide:4.13.2") {
        exclude(group = "com.android.support")
    }

    implementation("androidx.browser:browser:1.4.0")

    implementation("com.github.skydoves:only:1.0.8")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("androidx.preference:preference-ktx:1.2.0")

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

    implementation("com.android.billingclient:billing:4.1.0")
    implementation("com.android.billingclient:billing-ktx:4.1.0")

    implementation("com.facebook.android:facebook-login:13.1.0")

    implementation("com.github.adaptyteam:AdaptySDK-Android:1.9.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(group = "io.gleap", name = "gleap-android-sdk", version = "7.0.19")

    implementation("dev.bandb.graphview:graphview:0.8.1")
    implementation("com.otaliastudios:zoomlayout:1.9.0")

    implementation("androidx.compose.material:material-icons-extended")

}