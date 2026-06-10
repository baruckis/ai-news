plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.baruckis.ainews.feature.news"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

kover {
    // The aggregated root report applies the same filters, but per-module reports do NOT
    // inherit them and Codecov consumes this module's own XML — so the excludes are
    // repeated here, mirroring :core:network.
    reports {
        filters {
            excludes {
                classes(
                    // DI wiring (@Binds module) and its Dagger-generated companions;
                    // no testable logic.
                    "com.baruckis.ainews.feature.news.di.*",
                    "*_Factory",
                    "*_Factory\$*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                    "hilt_aggregated_deps.*",
                    "dagger.hilt.*",
                    "*Hilt_*",
                )
            }
        }
    }
}

dependencies {
    // Feature modules may use every core module; cores never depend back on features.
    implementation(project(":core:model"))
    implementation(project(":core:mvi"))
    implementation(project(":core:network"))
    implementation(project(":core:designsystem"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Repository tests run the generated operations against a real HTTP mock server,
    // so the Apollo runtime is needed on the test classpath (it is `implementation`
    // inside :core:network and therefore not visible here at compile time).
    testImplementation(libs.apollo.runtime)
    testImplementation(libs.apollo.mockserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
