plugins {
    id("java")
}

group = "dev.akarah"
version = "unspecified"

repositories {
    mavenCentral()

    maven {
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":codetemplates"))
}

tasks.test {
    useJUnitPlatform()
}