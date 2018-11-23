plugins {
    kotlin("jvm") version "1.3.10" apply false
}

allprojects {
    repositories {
        jcenter() 
    }
}

subprojects {
    version = "1.0"
}
