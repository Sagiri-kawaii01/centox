plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.github.sagiri_kawaii01"
version = "0.0.1-preview"

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
    jvmToolchain(8)
}