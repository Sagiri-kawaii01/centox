plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
}

group = "io.github.sagiri-kawaii01"
version = "0.1.3-preview"

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
    compileOnly("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    api("net.bytebuddy:byte-buddy:1.14.4")
    api("net.bytebuddy:byte-buddy-agent:1.14.4")
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
            artifactId = "centox-mp-kt"
            version = "0.1.3-preview"
            from(components["kotlin"])

            artifact(sourceJar)
            artifact(docJar)
            pom {
                name = "centox-mp-kt"
                description = "centox-mp-kt"
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