plugins {
    kotlin("jvm")
}

group = "io.github.sagiri_kawaii01"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:2.7.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:2.7.0")
    compileOnly("com.alibaba.fastjson2:fastjson2:2.0.50")
    implementation(project(":centox-mp-kt"))
    implementation(project(":centox-orika-kt"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

