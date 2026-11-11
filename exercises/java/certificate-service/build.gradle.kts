plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories { mavenCentral() }

// Boot 3.4.4 manages Testcontainers 1.20.6, which Docker Engine 29+ rejects
// (client API version too old). Remove this override once Boot manages >= 1.21.4.
extra["testcontainers.version"] = "1.21.4"

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    implementation("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // PDF generation and manipulation
    implementation("org.apache.pdfbox:pdfbox:3.0.4")

    // Security and cryptography
    implementation("org.bouncycastle:bcprov-jdk18on:1.87")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.87")
    implementation("org.bouncycastle:bcutil-jdk18on:1.87")

    // QR code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // Metrics and monitoring
    implementation("io.micrometer:micrometer-core")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("net.jqwik:jqwik:1.8.4")
}

tasks.bootJar { 
    archiveFileName.set("app.jar") 
}

// Configure test task
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

    // Disable class data sharing
    jvmArgs("-Xshare:off")
    
    // Increase heap size for tests
    minHeapSize = "256m"
    maxHeapSize = "1g"
    
    // Show standard out/err from tests
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

// Classes left out of coverage: wiring and data holders with no logic to test
val coverageExclusions = listOf("**/Application.class", "**/config/**", "**/model/**")

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )
}

// Opt-in: run `./gradlew jacocoTestCoverageVerification`. Deliberately not part of `check`,
// so an exercise that adds untested code does not fail the build.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    // Measure the same classes as the report
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it) { exclude(coverageExclusions) } })
    )

    violationRules {
        // Measured on 2026-11-11: 84% line, 72% branch; lowest service class PdfBoxGenerator at 63%.
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.78".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
        }

        rule {
            element = "CLASS"
            includes = listOf("com.kousen.cert.service.*")

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}
