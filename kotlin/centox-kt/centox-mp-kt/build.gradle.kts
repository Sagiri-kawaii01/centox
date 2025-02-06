plugins {
    kotlin("jvm")
}

group = "io.github.sagiri_kawaii01"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    implementation("net.bytebuddy:byte-buddy:1.14.4")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}