plugins {
    java
    application

    antlr
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("io.freefair.lombok") version "4.1.6"
}

repositories {
    jcenter()
}

dependencies {
    antlr("org.antlr:antlr4:4.8-1")

    implementation("org.apache.logging.log4j:log4j-api:2.13.1")
    implementation("org.apache.logging.log4j:log4j-core:2.13.1")
    implementation("com.google.dagger:dagger:2.26")
    annotationProcessor("com.google.dagger:dagger-compiler:2.26")
    implementation("org.apache.commons:commons-text:1.8")
    implementation("org.apache.commons:commons-lang3:3.9")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.apache.commons:commons-collections4:4.4")

    implementation("com.google.guava:guava:28.2-jre")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    implementation(project(":ipid-util"))
}

lombok {
    version.set("1.18.+")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.generateGrammarSource {
    arguments = arguments + listOf(
        "-visitor", "-no-listener",
        "-package", "me.ipid.jamelin.thirdparty.antlr"
    )
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

application {
    mainClassName = "me.ipid.jamelin.Main"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
