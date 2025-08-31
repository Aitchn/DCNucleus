plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.aitchn.dcnucleus"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}