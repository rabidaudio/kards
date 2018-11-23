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
