plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Pure Kotlin/JVM domain module. By design it depends on nothing else.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
