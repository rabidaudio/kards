import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    application
}

application {
    mainClassName = "audio.rabid.kards.gofish.MainKt"
}

dependencies {
    compile(project(":core"))
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.0.0-RC11")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-alpha.1") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-alpha.1") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly(kotlin("reflect"))
    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testImplementation(project(":test-utils"))
}

detekt {
    config = files("$rootDir/default-detekt-config.yml")
    filters = ".*build.*,.*/resources/.*,.*/tmp/.*"
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
//        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
}
