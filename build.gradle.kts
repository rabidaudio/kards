plugins {
    kotlin("jvm") version "1.3.10" apply false
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
