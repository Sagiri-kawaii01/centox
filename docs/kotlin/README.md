# Centox For Kotlin

## 依赖

> libs.versions.toml

```toml
[versions]
springBoot="2.7.17"
springDependencyManagement="1.1.4"
springPlugin="1.6.21"

# kt & ksp 使用 2.0+ 编译更快
ksp="1.9.21-1.0.15"
kotlin="1.9.21"

# Mp
mysql="8.0.29"
mybatisPlus="3.5.2"
mybatisPlusGenerator="3.5.1"
velocity="2.0"
pagehelper="1.4.7"

centox="1.0.0"
jsonschema="4.33.1"

[libraries]
spring-boot-starter-web = { group = "org.springframework.boot", name = "spring-boot-starter-web" }
spring-boot-starter-aop = { group = "org.springframework.boot", name = "spring-boot-starter-aop" }
spring-boot-starter-test = { group = "org.springframework.boot", name = "spring-boot-starter-test" }
spring-boot-starter-validation = { group = "org.springframework.boot", name = "spring-boot-starter-validation" }
spring-boot-starter-redis = { group = "org.springframework.boot", name = "spring-boot-starter-data-redis" }
spring-boot-starter-jdbc = { group = "org.springframework.boot", name = "spring-boot-starter-data-jdbc" }

jdbc-mysql = { group = "mysql", name = "mysql-connector-java", version.ref = "mysql" }
mybatis-plus = { group = "com.baomidou", name = "mybatis-plus-boot-starter", version.ref = "mybatisPlus" }
mybatis-plus-generator = { group = "com.baomidou", name = "mybatis-plus-generator", version.ref = "mybatisPlusGenerator" }
velocity-engine = { group = "org.apache.velocity", name = "velocity-engine-core", version.ref = "velocity" }
pagehelper = { group = "com.github.pagehelper", name = "pagehelper-spring-boot-starter", version.ref = "pagehelper" }


jsonschema = { group = "com.github.victools", name = "jsonschema-generator", version.ref = "jsonschema" }
jsonschema-jackson = { group = "com.github.victools", name = "jsonschema-module-jackson", version.ref = "jsonschema" }
jsonschema-validation = { group = "com.github.victools", name = "jsonschema-module-javax-validation", version.ref = "jsonschema" }

centox = { group = "io.github.sagiri-kawaii01", name = "spring-boot-starter-centox", version.ref = "centox" }
centox-kt = { group = "io.github.sagiri-kawaii01", name = "centox-starter-kt", version.ref = "centox" }
centox-mp-kt = { group = "io.github.sagiri-kawaii01", name = "centox-mp-kt", version.ref = "centox" }
centox-ksp-auth = { group = "io.github.sagiri-kawaii01", name = "centox-ksp-auth", version.ref = "centox" }
centox-ksp-openapi = { group = "io.github.sagiri-kawaii01", name = "centox-ksp-openapi", version.ref = "centox" }

```

> build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

springBoot {
    mainClass = "xxx.xxx.xxx.MainKt"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

// 文档生成
ksp {
    val paths = listOf(
        "${projectDir.absoluteFile}\\src\\main\\resources"
    ).joinToString("|")
    arg("translate.yml.paths", paths)
    arg("packageName", "包名")
    arg("projectName", "项目名称")
    arg("version", "1.0")
}

dependencies {
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.test)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.redis)
    implementation(libs.spring.boot.starter.jdbc)
    
    implementation(libs.centox)
    implementation(libs.centox.kt)
    implementation(libs.centox.mp.kt)
    implementation(libs.centox.ksp.auth)
    implementation(libs.centox.ksp.openapi)
    ksp(libs.centox.ksp.auth)
    ksp(libs.centox.ksp.openapi)
    
    implementation(libs.mybatis.plus)
    implementation(libs.mybatis.plus.generator)
    implementation(libs.jdbc.mysql)
    implementation(libs.pagehelper)
    implementation(libs.velocity.engine)
    
    implementation(libs.jsonschema)
    implementation(libs.jsonschema.jackson)
    implementation(libs.jsonschema.validation)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

```



## 启动

```kotlin
@SpringBootApplication
class Main

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}
```



## 其他

* [MybatisPlus](./mp.md)
