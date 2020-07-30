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
    implementation(project(":core"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.10.0")
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
