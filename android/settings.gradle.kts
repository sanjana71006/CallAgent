pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Fix for AndroidLocationsBuildService error on Windows/OneDrive
// This needs to be set early in the configuration phase
System.setProperty("android.user.home", "C:\\AgentProject\\.android")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CallMate AI"
include(":app")
