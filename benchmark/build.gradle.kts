plugins {
    alias(libs.plugins.android.test)
    // Producer side: turns the BaselineProfileGenerator's output into an artifact :app consumes.
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.baruckis.ainews.benchmark"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // Baseline profiles need API 28+; the GMD image below is what actually runs.
        minSdk = 28
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Benchmarks run on the managed emulator: absolute numbers are not device-grade,
        // but before/after comparisons on the same image are valid. Without this flag
        // Macrobenchmark refuses to measure on an emulator at all.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Self-instrumenting test module: measures :app's release-like variants.
    targetProjectPath = ":app"

    testOptions {
        managedDevices {
            localDevices.create("pixel6Api34") {
                device = "Pixel 6"
                apiLevel = 34
                // AOSP (rooted) image: required for baseline-profile extraction and
                // CI-friendly (no physical device, no Google APIs license prompts).
                systemImageSource = "aosp"
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

baselineProfile {
    // Generate on the managed device so CI and local runs are reproducible without
    // a physical device.
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.junit4)
}
