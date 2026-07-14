plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mockmp)
    id("io.lb.kotlin.multiplatform")
    id("io.lb.compose.multiplatform")
    id("io.lb.android.library.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation.compose)
            implementation(compose.materialIconsExtended)
            implementation(projects.feature.meals.domain)
            implementation(projects.core.common)
            implementation(projects.core.designsystem)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.androidx.test.runner)
        }
    }
}

dependencies {
    debugImplementation(libs.compose.ui.test.manifest)
}

android {
    namespace = "io.lb.lbmealsnew.feature.meals.presentation"
}

mockmp {
    onTest()
}
