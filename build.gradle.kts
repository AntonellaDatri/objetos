import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.72"
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.3.71"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.71"
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.spring") version "1.4.30"
    id("java")
    war

}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = "ar.edu.unq.eperdemic"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("mysql:mysql-connector-java")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("junit:junit:4.12")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("org.neo4j.driver:neo4j-java-driver:4.0.1")
    implementation("org.hibernate:hibernate-core:5.4.12.Final")
    implementation("org.neo4j.driver:neo4j-java-driver:4.0.1")
    testImplementation("com.h2database:h2")

    implementation("org.mongodb:mongodb-driver-sync:4.1.1")
    testImplementation(group= "org.slf4j", name= "slf4j-log4j12", version= "1.7.30")
    implementation(group= "org.apache.logging.log4j", name= "log4j-core", version= "2.13.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
