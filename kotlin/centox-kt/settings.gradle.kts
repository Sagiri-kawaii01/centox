plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "centox-kt"
include("centox-mp-kt")
include("spring-boot-starter-centox-kt")
include("centox-orika-kt")
include("centox-orika")
include("centox-ksp-auth")
include("centox-ksp-openapi")
