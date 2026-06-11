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

rootProject.name = "ai-news"

include(":app")
include(":core:model")
include(":core:mvi")
include(":core:network")
include(":core:designsystem")
include(":core:testing")
include(":feature:news")
include(":bff")
// Macrobenchmarks + baseline-profile generator for :app (instrumentation, not unit-tested code).
include(":benchmark")
