plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.9.20"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.20")
    implementation("org.flywaydb:flyway-gradle-plugin:10.17.2")
    implementation("org.flywaydb:flyway-mysql:10.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
