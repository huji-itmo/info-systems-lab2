plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.allopen") version "2.2.20"
    kotlin("plugin.noarg") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("jakarta.platform:jakarta.jakartaee-api:11.0.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.20.1")

    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.0")
    implementation("org.glassfish.jersey.media:jersey-media-multipart:4.0.0")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework

            useKotlinTest("1.9.22")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

allOpen {
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.enterprise.context.RequestScoped")
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.persistence.Entity")
}

noArg {
    annotation("jakarta.enterprise.context.RequestScoped")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("org.example.JsonDeserializable")
    annotation("jakarta.persistence.Entity")
}
