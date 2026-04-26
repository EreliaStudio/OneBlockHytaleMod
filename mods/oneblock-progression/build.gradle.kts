base {
    archivesName.set("OneBlock-Progression")
}

dependencies {
    compileOnly(project(":oneblock-core"))
}

tasks.named("compileJava") {
    dependsOn(":oneblock-core:shadowJar")
}
