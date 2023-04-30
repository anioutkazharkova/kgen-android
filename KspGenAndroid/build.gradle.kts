
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.3.1").apply(false)
    id("com.android.library").version("7.3.1").apply(false)
    kotlin("android").version("1.8.0").apply(false)
    id("org.jetbrains.kotlin.jvm") version "1.8.0" apply false
    id("com.google.devtools.ksp") version "1.8.0-1.0.8" apply false
 // id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}
