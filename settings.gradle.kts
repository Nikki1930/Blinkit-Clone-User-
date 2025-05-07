pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(url = "https://jitpack.io")
        maven(url = "https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android")
        mavenCentral()
    }
}

rootProject.name = "UserBlinkitClone"
include(":app")
