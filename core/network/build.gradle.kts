import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ksp)
}

// GRAPHQL_URL is read from local.properties (never committed). The emulator-localhost
// default is only a fallback so the project still builds without a local.properties file.
val graphqlUrl: String =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }.getProperty("GRAPHQL_URL") ?: "http://10.0.2.2:8080/graphql"

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
