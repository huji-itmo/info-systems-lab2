plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.allopen") version "2.2.20" apply false
    kotlin("plugin.noarg") version "2.2.20" apply false
    kotlin("plugin.jpa") version "2.2.20" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
}



rootProject.name = "info-systems-lab2"
include("file-service", "payara-service", "library-shared")
