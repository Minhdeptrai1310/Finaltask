pluginManagement {
    repositories {
        google() // Để Google repo ở đây
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Sửa từ FAIL_ON_PROJECT_REPOS sang PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Finaltask"
include(":app")
