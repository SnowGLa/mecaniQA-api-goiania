plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.mecaniQA"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	useJUnitPlatform { excludeTags("mysql") }
}

tasks.register<Test>("mysqlIntegrationTest") {
	description = "Executa testes opcionais contra MySQL real configurado por MYSQL_TEST_*."
	group = "verification"
	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	useJUnitPlatform { includeTags("mysql") }
	onlyIf { System.getenv("MYSQL_TEST_URL") != null && System.getenv("MYSQL_TEST_USERNAME") != null && System.getenv("MYSQL_TEST_PASSWORD") != null }
}
