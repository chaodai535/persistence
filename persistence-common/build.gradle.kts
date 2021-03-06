import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.4.20"
    maven
    id("org.springframework.boot") version "2.4.0"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
}

repositories {
    mavenCentral()
}

dependencies {
    val arrowVersion = "0.11.0"
    arrow(arrowVersion)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.amazonaws:aws-java-sdk-s3:1.11.210")

    implementation("org.jooq:joor-java-8:0.9.12")
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation("commons-io:commons-io:2.8.0")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5")

    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.github.czyzby:kotlin-times:1.0")

    implementation("org.jxls:jxls:2.4.0")
    implementation("org.jxls:jxls-poi:1.0.12")
    implementation("org.jxls:jxls-jexcel:1.0.6")
    implementation("org.jxls:jxls-reader:2.0.2")
    implementation("com.google.guava:guava:30.0-jre")

    implementation("org.apache.commons:commons-lang3:3.11")


    compileOnly("javax.servlet:javax.servlet-api:3.1.0")

}
fun DependencyHandlerScope.arrow(arrowVersion: String) {
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
