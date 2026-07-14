rootProject.name = "LBMeals-New"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core:common")
include(":core:network")
include(":core:database-api")
include(":core:database-sqldelight")
include(":core:designsystem")
include(":feature:categories:domain")
include(":feature:categories:data")
include(":feature:categories:presentation")
include(":feature:meals:domain")
include(":feature:meals:data")
include(":feature:meals:presentation")
