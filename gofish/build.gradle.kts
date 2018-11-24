import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

application {
    mainClassName = "audio.rabid.kards.gofish.MainKt"
}

dependencies {
    compile(project(":core"))
    compile(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses -Xuse-experimental=kotlin.contracts.ExperimentalContracts")
    }
}
