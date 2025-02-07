plugins {
    kotlin("jvm")
}

group = "io.github.sagiri-kawaii01"
version = "0.0.2-preview"

repositories {
    mavenCentral()
}

dependencies {
    api("ma.glasnost.orika:orika-core:1.5.4")
    api("com.alibaba.fastjson2:fastjson2-kotlin:2.0.14")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}