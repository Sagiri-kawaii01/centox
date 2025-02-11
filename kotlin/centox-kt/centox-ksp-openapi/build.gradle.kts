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
    compileOnly("com.alibaba.fastjson2:fastjson2:2.0.14")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.0")
    api("com.github.victools:jsonschema-generator:4.33.1")
    api("com.github.victools:jsonschema-module-jackson:4.33.1")
    api("com.github.victools:jsonschema-module-javax-validation:4.33.1")
    api("javax.validation:validation-api:2.0.1.Final")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("io.github.sagiri-kawaii01:centox-core:1.0.0")
    api("io.github.sagiri-kawaii01:centox-log:1.0.0")
    api("io.github.sagiri-kawaii01:centox-mp:1.0.0")
    api("org.reflections:reflections:0.10.2")
    api(kotlin("reflect"))
    testImplementation(kotlin("test"))
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
            artifactId = "centox-ksp-openapi"
            version = "1.0.0"
            from(components["kotlin"])

            artifact(sourceJar)
            artifact(docJar)
            pom {
                name = "centox-ksp-openapi"
                description = "centox-ksp-openapi"
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