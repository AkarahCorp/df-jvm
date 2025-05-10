plugins {
    id("java")
}

group = "dev.akarah"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.mojang:datafixerupper:8.0.16")
    implementation("com.google.code.gson:gson:2.13.1")
}

tasks.test {
    useJUnitPlatform()
}