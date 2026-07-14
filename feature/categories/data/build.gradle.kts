plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mockmp)
    id("io.lb.kotlin.multiplatform")
    id("io.lb.android.library.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(projects.feature.categories.domain)
            implementation(projects.core.network)
            implementation(projects.core.databaseApi)
        }
    }
}

android {
    namespace = "io.lb.lbmealsnew.feature.categories.data"
}

mockmp {
    onTest()
}
