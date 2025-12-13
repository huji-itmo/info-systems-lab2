plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "info-systems-lab2"
include("file-service", "payara-service")
