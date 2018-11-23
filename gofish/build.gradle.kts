plugins {
    java        
    application 
}

application {
    mainClassName = "audio.rabid.kards.gofish.Main" 
}

dependencies {
    compile(project(":core")) 
}
