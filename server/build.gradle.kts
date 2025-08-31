plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

group = "io.aitchn.dcnucleus.server"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":api"))
    implementation(project(":test-plugin")) // 測試用

    // === === Serialization === ===
    implementation("com.charleskorn.kaml:kaml:0.93.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")


    // === === Logger === ===
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
    runtimeOnly("org.fusesource.jansi:jansi:2.4.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}