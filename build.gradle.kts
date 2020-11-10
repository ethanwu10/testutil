plugins {
    `java-library`
    kotlin("jvm") version "1.4.10"
}

group = "dev.ethanwu"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("org.junit.jupiter", "junit-jupiter-api", "5.7.0")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
