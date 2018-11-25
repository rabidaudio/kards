import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    api(kotlin("stdlib-jdk8"))

    api("com.winterbe:expekt:0.5.0")
}
