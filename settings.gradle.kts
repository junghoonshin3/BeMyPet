@file:Suppress("UnstableApiUsage")

include(":feature:adoption-filter")



pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BeMyPet"
include(":app")
include(":core:firebase")
include(":core:google")
include(":core:designsystem")
include(":core:model")
include(":feature:login-register")
include(":feature:login")
include(":core:data")
include(":core:ktor")
include(":feature:chat")
include(":feature:review")
include(":feature:adoption")
include(":feature:mypage")
