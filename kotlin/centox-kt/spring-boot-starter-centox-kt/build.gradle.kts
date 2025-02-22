plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven-publish")
    id("signing")
}

group = "io.github.sagirikawaii01"

repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

tasks.withType<Jar> {
    // 设置不包含主类
    manifest.attributes.clear()
}
dependencies {
    testImplementation(kotlin("test"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.7.0")
    kapt("org.springframework.boot:spring-boot-configuration-processor:2.7.0")
    compileOnly("org.springframework.boot:spring-boot-starter-web:2.7.0")
    compileOnly("com.alibaba.fastjson2:fastjson2:2.0.50")
    compileOnly("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    api(project(":centox-mp-kt"))
    api(project(":centox-orika-kt"))
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val docJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets["main"].allSource)
}


publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "io.github.sagiri-kawaii01"
            artifactId = "centox-starter-kt"
            version = "1.0.0"
            from(components["kotlin"])

            artifact(sourceJar)
            artifact(docJar)
            pom {
                name = "centox-starter-kt"
                description = "centox-starter-kt"
                url = "https://github.com/Sagiri-kawaii01/centox"
                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
                developers {
                    developer {
                        id = "sagiri"
                        name = "sagiri-kawaii01"
                        email = "sagiri.wo@gmail.com"
                    }
                }
                scm {
                    url = "https://github.com/Sagiri-kawaii01/centox"
                }
            }
        }

        signing {
            useGpgCmd()
            sign(publishing.publications["mavenJava"])
        }

        repositories {
            maven {
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }

    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

