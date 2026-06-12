import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ksp)
}

// GRAPHQL_URL comes from the environment (release CI) or local.properties (never
// committed). The emulator-localhost default is only a fallback so the project still
// builds without either. This BuildConfig field is the one ApolloModule dials, so the
// environment lookup must live here — release.yml exports GRAPHQL_URL as an env var and
// CI has no local.properties.
val graphqlUrl: String =
    System.getenv("GRAPHQL_URL")
        ?: Properties()
            .apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }.getProperty("GRAPHQL_URL")
        ?: "http://10.0.2.2:8080/graphql"

android {
    namespace = "com.baruckis.ainews.core.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        buildConfigField("String", "GRAPHQL_URL", "\"$graphqlUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
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
    // repeated here, mirroring how :bff excludes its composition root locally.
    reports {
        filters {
            excludes {
                classes(
                    "com.baruckis.ainews.core.network.BuildConfig",
                    // Apollo codegen output (queries, fragments, adapters) — generated code.
                    "com.baruckis.ainews.core.network.graphql.*",
                    // DI wiring (Android Context + SQLite cache) and its Dagger-generated
                    // Module_Provide*Factory companions; no testable logic.
                    "com.baruckis.ainews.core.network.di.*",
                    "*_Factory",
                    "hilt_aggregated_deps.*",
                    "dagger.hilt.*",
                    "*Hilt_*",
                )
            }
        }
    }
}

apollo {
    service("news") {
        packageName.set("com.baruckis.ainews.core.network.graphql")
        // The BFF serves DateTime as an ISO-8601 instant; expose it as java.time.Instant.
        mapScalar(
            "DateTime",
            "java.time.Instant",
            "com.apollographql.adapter.core.JavaInstantAdapter",
        )
        generateOptionalOperationVariables.set(false)
    }
}

dependencies {
    // Network layer maps onto domain models; it never depends on UI.
    implementation(project(":core:model"))

    // Generated operations and the GqlApiLayer signature surface apollo-api types
    // (Query, Query.Data) to the calling data layer; the runtime stays internal.
    api(libs.apollo.api)
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.normalized.cache.sqlite)
    implementation(libs.apollo.adapters.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.apollo.mockserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
