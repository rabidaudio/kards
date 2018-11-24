import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.0-alpha.1")  {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.0-alpha.1") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly(kotlin("reflect"))
    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.contracts.ExperimentalContracts")
    }
}
