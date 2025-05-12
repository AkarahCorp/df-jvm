plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "dev.akarah"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":df-jvm-api"))
}

tasks.test {
    useJUnitPlatform()
}