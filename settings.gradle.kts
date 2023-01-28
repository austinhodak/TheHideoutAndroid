pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    versionCatalogs {
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
    }
}

include(":app")
rootProject.name = "The Hideout"
include(":tarkovapi")
include(":mp-core")
include(":mp-library-datastore")
include(":mp-library-preference-screen")
include(":mp-library-preference-screen-color")
include(":mp-library-preference-screen-input")
include(":mp-library-preference-screen-choice")
