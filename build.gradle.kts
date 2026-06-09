// Pin the Kotlin version on the build classpath so AGP 9's built-in Kotlin uses 2.3.21
// instead of the version bundled with the Android Gradle plugin.
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

// Aggregate coverage from every module into the root Kover report.
// :app is intentionally omitted — in Stage 0 it contains only entry points
// (Application, MainActivity) and generated Hilt/Compose code, none of which is testable logic.
dependencies {
    kover(project(":core:model"))
    kover(project(":core:mvi"))
    kover(project(":core:network"))
    kover(project(":core:designsystem"))
    kover(project(":core:testing"))
    kover(project(":feature:news"))
    kover(project(":bff"))
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        autoCorrect = false
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        ignoreFailures.set(false)
    }
}

kover {
    reports {
        filters {
            excludes {
                // Android entry points and generated code carry no testable logic.
                // Real composables are NOT excluded: the design-system components are
                // exercised by the Roborazzi screenshot tests, so they are measured here.
                classes(
                    "*.BuildConfig",
                    "com.baruckis.ainews.AiNewsApplication",
                    "com.baruckis.ainews.MainActivity",
                    "com.baruckis.ainews.MainActivity\$*",
                    // BFF composition root: server bootstrap + environment wiring, no testable logic.
                    "com.baruckis.ainews.bff.MainKt",
                    "com.baruckis.ainews.bff.MainKt\$*",
                    "*ComposableSingletons*",
                    "*_Factory",
                    "*_Factory\$*",
                    "*_HiltModules*",
                    "*_MembersInjector",
                    "hilt_aggregated_deps.*",
                    "dagger.hilt.*",
                    "*Hilt_*",
                    // Preview-only helper: rendered solely by @Preview, never at runtime.
                    "*.designsystem.components.PreviewSupportKt",
                )
                annotatedBy("dagger.hilt.android.HiltAndroidApp")
            }
        }
        total {
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}
