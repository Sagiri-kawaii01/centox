plugins {
    id("java")
    id("maven-publish")
    id("signing")
}

group = "io.github.sagiri-kawaii01"
version = "1.0.0"

repositories {
    mavenCentral()
}


sourceSets.main {
    java.srcDirs("src/main/java")
}

tasks.withType<Jar> {
    // 设置不包含主类
    manifest.attributes.clear()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}



dependencies {
    compileOnly("ma.glasnost.orika:orika-core:1.5.4")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
            artifactId = "centox-orika"
            version = "1.0.0"
            from(components["java"])

            pom {
                name = "centox-orika"
                description = "centox-orika"
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}