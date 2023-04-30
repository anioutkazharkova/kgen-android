plugins {
    id("com.google.devtools.ksp")
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}



dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.8")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.squareup:kotlinpoet:1.12.0")
}