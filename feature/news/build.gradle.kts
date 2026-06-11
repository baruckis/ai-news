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
        unitTests {
            // Robolectric needs the merged resources/manifest to inflate Compose content.
            isIncludeAndroidResources = true
            all { test ->
                // Jupiter runs the JVM unit tests; the vintage engine runs the JUnit 4
                // Robolectric/Roborazzi UI tests on the same JUnit Platform launcher.
                test.useJUnitPlatform()
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
    // Marks the pure-Kotlin :core:model classes (and java.time.Instant) as stable without
    // adding a Compose dependency to the domain module.
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("config/compose/stability.conf"))
    // Opt-in (slows compilation): -PcomposeCompilerReports writes the stability/skippability
    // reports consumed into docs/compose-metrics/.
    if (project.hasProperty("composeCompilerReports")) {
        metricsDestination = layout.buildDirectory.dir("compose-compiler")
        reportsDestination = layout.buildDirectory.dir("compose-compiler")
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
                    // Preview-only files (@Preview light/dark wrappers and their sample
                    // data): rendered solely by Android Studio, never at runtime. The
                    // runtime composables they wrap stay measured via Robolectric tests.
                    "*PreviewsKt",
                    "*ComposableSingletons*",
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

    // ImmutableList is part of the public UiState API, so consumers need it on their
    // compile classpath too.
    api(libs.kotlinx.collections.immutable)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    // Material3 no longer bundles the icon set; the detail top bar uses the back arrow.
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Article images: Coil 3 compose integration + OkHttp fetcher for http(s) URLs.
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Repository tests run the generated operations against a real HTTP mock server,
    // so the Apollo runtime is needed on the test classpath (it is `implementation`
    // inside :core:network and therefore not visible here at compile time).
    testImplementation(libs.apollo.runtime)
    testImplementation(libs.apollo.mockserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    // Robolectric + Roborazzi UI tests are JUnit 4; vintage runs them on the platform.
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
