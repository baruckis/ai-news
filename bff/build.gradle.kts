import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

group = "com.baruckis.ainews"
version = "0.1.0"

application {
    // Entry point started by `gradlew :bff:run` and packaged by `buildFatJar`.
    // `fun main()` lives in Main.kt, which compiles to MainKt.
    mainClass.set("com.baruckis.ainews.bff.MainKt")
}

kover {
    // Main.kt is the composition root (server bootstrap + environment wiring) with no
    // testable logic, mirroring how the :app entry points are excluded in the root build.
    // Excluding it here keeps it out of this module's own report too, so it never counts
    // against patch coverage regardless of which report is consumed.
    reports {
        filters {
            excludes {
                classes(
                    "com.baruckis.ainews.bff.MainKt",
                    "com.baruckis.ainews.bff.MainKt\$*",
                )
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.graphql.kotlin.ktor.server)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
