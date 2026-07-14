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
            api(projects.core.common)
        }
    }
}

android {
    namespace = "io.lb.lbmealsnew.feature.meals.domain"
}

mockmp {
    onTest()
}
