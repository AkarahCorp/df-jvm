plugins {
    id("java")
    application
}

group = "dev.akarah"
version = "unspecified"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

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

application {
    mainClass.set("dev.akarah.dfjvm.compiler.Main")
}

tasks.run<JavaExec> {
    args(listOf("${project.projectDir}/../df-jvm-example/build/libs/df-jvm-example-all.jar"))
}