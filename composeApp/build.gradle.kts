plugins {
    id("io.lb.android.app.multiplatform")
    id("io.lb.kotlin.multiplatform")
    id("io.lb.compose.multiplatform")
}

android {
    namespace = "io.lb.lbmealsnew"

    defaultConfig {
        applicationId = "io.lb.lbmealsnew"
        versionCode = 1
        versionName = "1.0"
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation.compose)
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.databaseApi)
            implementation(projects.core.databaseSqldelight)
            implementation(projects.core.designsystem)
            implementation(projects.feature.categories.domain)
            implementation(projects.feature.categories.data)
            implementation(projects.feature.categories.presentation)
            implementation(projects.feature.meals.domain)
            implementation(projects.feature.meals.data)
            implementation(projects.feature.meals.presentation)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
