@file:Suppress("UnstableApiUsage")

include(":feature:block")


include(":feature:report")


include(":feature:report")


include(":feature:comments")


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
include(":core:designsystem")
include(":core:model")
include(":core:data")
include(":core:ktor")
include(":feature:adoption")
include(":core:database")
include(":feature:favourite")
include(":feature:adoption-detail")
include(":core:common")
include(":feature:setting")
include(":feature:signIn")
include(":core:datastore")
include(":core:supabase")

