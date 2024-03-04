import org.jetbrains.kotlin.daemon.common.ensureServerHostnameIsSetUp
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile



group = "ru.svrd"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}


val allureVersion = "2.24.0"
val aspectJVersion = "1.9.20.1"
val agent: Configuration by configurations.creating {
	isCanBeConsumed = true
	isCanBeResolved = true
}

plugins {
	id("org.springframework.boot") version "3.2.0"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
	id("io.qameta.allure") version "2.11.2"
	id("io.qameta.allure-report") version "2.11.2"
}

//plugins {
//	kotlin("jvm") version "1.9.20"
//	id("io.qameta.allure") version "2.11.2"
//	id("io.qameta.allure-report") version "2.11.2"
//}

reporting {
	baseDir = File("allureReports")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:testcontainers:1.19.3")
	testImplementation("org.testcontainers:postgresql:1.19.3")
	testImplementation("org.testcontainers:junit-jupiter:1.19.3")

	testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
	testImplementation("io.qameta.allure:allure-junit5")
	implementation("io.qameta.allure:allure-commandline:2.19.0")

	agent("org.aspectj:aspectjweaver:${aspectJVersion}")

}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

//allure {
//	version.set("2.19.0")
//	ensureServerHostnameIsSetUp()
//}

tasks.withType<Test> {
	useJUnitPlatform()
}
