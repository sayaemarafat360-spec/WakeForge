plugins {
    id("com.android.application") version libs.versions.agp apply false
    id("com.android.library") version libs.versions.agp apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin apply false
    id("org.jetbrains.kotlin.kapt") version libs.versions.kotlin apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version libs.versions.ksp apply false
}
