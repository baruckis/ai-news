plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.baruckis.ainews.core.designsystem"
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
        unitTests {
            // Robolectric needs the merged resources/manifest to inflate Compose content.
            isIncludeAndroidResources = true
            all { test ->
                // Default runs VERIFY each screenshot against its committed reference and
                // fail on any difference. Pass -Precord to (re)generate the references.
                val roborazziMode = if (project.hasProperty("record")) "record" else "verify"
                test.systemProperty("roborazzi.test.$roborazziMode", "true")
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

composeCompiler {
    // Strong skipping (skippable composables even with unstable params, memoized lambdas)
    // is enabled by default on this compiler version; the compiler warns if it is also
    // requested explicitly, so it is not repeated here. The reports under
    // docs/compose-metrics/ confirm it is active.
    // Opt-in (slows compilation): -PcomposeCompilerReports writes the stability/skippability
    // reports consumed into docs/compose-metrics/.
    if (project.hasProperty("composeCompilerReports")) {
        metricsDestination = layout.buildDirectory.dir("compose-compiler")
        reportsDestination = layout.buildDirectory.dir("compose-compiler")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
