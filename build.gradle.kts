plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "uz.consortgroup"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}


repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

dependencies {
    // Swagger (SpringDoc) -
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // Feign (REST Clients)
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign.form:feign-form-spring:3.8.0")

    //Eureka
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // core-api-dto
    implementation("uz.consortgroup:core-api-dto:0.0.1")

    //Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MapStruct
    compileOnly("org.mapstruct:mapstruct-processor:1.5.3.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.3.Final")
    implementation("org.mapstruct:mapstruct:1.5.3.Final")

    // JWT
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring data
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    //Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Apache Kafka
    implementation("org.springframework.kafka:spring-kafka:3.2.0")

    //Database
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    // Тестирование
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.15.11")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()

    doFirst {
        val agent = configurations.testRuntimeClasspath.get()
            .find { it.name.contains("byte-buddy-agent") }
        if (agent != null) {
            jvmArgs("-javaagent:${agent.absolutePath}")
        }
    }

    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

