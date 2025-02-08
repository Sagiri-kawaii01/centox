plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
}

group = "io.github.sagiri-kawaii01"
version = "0.0.5-preview"

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
