plugins {
    kotlin("jvm") version "1.3.70" apply false
    id("io.gitlab.arturbosch.detekt") version "1.10.0" apply false
}

allprojects {
    repositories {
        jcenter()
        maven("https://dl.bintray.com/spekframework/spek-dev")
    }
}

subprojects {
    version = "1.0"
}
