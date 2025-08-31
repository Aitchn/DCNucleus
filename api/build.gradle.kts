plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.aitchn.dcnucleus"
version = "unspecified"

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