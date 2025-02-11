plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
}

group = "io.github.sagiri-kawaii01"
version = "1.0.0"

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
    api("ma.glasnost.orika:orika-core:1.5.4")
    api("com.alibaba.fastjson2:fastjson2-kotlin:2.0.14")
    testImplementation(kotlin("test"))
    api(project(":centox-orika"))
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
            artifactId = "centox-orika-kt"
            version = "1.0.0"
            from(components["kotlin"])

            artifact(sourceJar)
            artifact(docJar)
            pom {
                name = "centox-orika-kt"
                description = "centox-orika-kt"
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