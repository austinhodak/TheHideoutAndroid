@file:Suppress("DSL_SCOPE_VIOLATION")

import org.slf4j.LoggerFactory

plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.kapt).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.dagger.hilt.android).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.gms.googleServices).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.secrets).apply(false)
    alias(libs.plugins.realm).apply(false)
    alias(libs.plugins.firebase.perf).apply(false)
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                // Avoid having to stutter experimental annotations all over the codebase
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
                "-opt-in=androidx.compose.runtime.ExperimentalComposeApi",
                "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                "-opt-in=com.google.accompanist.pager.ExperimentalPagerApi",
                "-opt-in=kotlin.ExperimentalUnsignedTypes",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }

}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Version should be inherited from parent

    repositories {
        // Required to download KtLint
        mavenCentral()
    }

    // Optionally configure plugin
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
    }
}

val loggerFactory: org.slf4j.ILoggerFactory = LoggerFactory.getILoggerFactory()
val addNoOpLogger: java.lang.reflect.Method =
    loggerFactory.javaClass.getDeclaredMethod("addNoOpLogger", String::class.java)
addNoOpLogger.isAccessible = true
addNoOpLogger.invoke(
    loggerFactory,
    "com.android.build.api.component.impl.MutableListBackedUpWithListProperty"
)
addNoOpLogger.invoke(
    loggerFactory,
    "com.android.build.api.component.impl.MutableMapBackedUpWithMapProperty"
)