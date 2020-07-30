import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.10.0")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.12") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.12") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly(kotlin("reflect"))
    testImplementation("org.junit.platform:junit-platform-engine:1.7.0-M1")
    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testImplementation(project(":test-utils"))
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek2")
    }
    testLogging.lifecycle {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events = TestLogEvent.values().toSet() - TestLogEvent.STARTED
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.contracts.ExperimentalContracts")
    }
}
