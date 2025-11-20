plugins {
    id("war")
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.allopen") version "2.2.20"
    kotlin("plugin.noarg") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Jakarta EE API - compileOnly since provided by Payara Micro
    compileOnly("jakarta.platform:jakarta.jakartaee-api:11.0.0")

    // EclipseLink (JPA) - Payara provides this, but you may need it for compilation
    compileOnly("org.eclipse.persistence:eclipselink:4.0.8")

    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.7.3")

    // Jackson for JSON processing - REQUIRED for JSON serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.20.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.20.1")

    // Jersey-Jackson integration for Payara Micro
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.0")


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

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
