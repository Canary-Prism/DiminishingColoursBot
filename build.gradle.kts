
plugins {
    application
    id("com.gradleup.shadow") version "8.3.5"
    id("org.hidetake.ssh") version "2.11.2"
}

group = "canaryprism"
version = "1.3.0"

application {
    mainClass = "diminishingcoloursbot.Main"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.javacord:javacord:3.8.0")
//    implementation("net.sourceforge.htmlunit:htmlunit:2.70.0")
    implementation("dev.kdrag0n:colorkt:1.0.5")
    implementation("org.json:json:20230227")
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier = null
}

val mia_deploy = file("/Users/mia/Documents/mia-deploy.gradle")
if (mia_deploy.exists()) apply(mia_deploy)