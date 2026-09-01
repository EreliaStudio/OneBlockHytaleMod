import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

base {
    archivesName.set("OneBlockIslands")
}

dependencies {
    compileOnly(project(":oneblock"))
    testImplementation(files(rootProject.file("libs/HytaleServer.jar")))
}

tasks.named<Test>("test") {
    dependsOn(project(":oneblock").tasks.named("shadowJar"))
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("com.google.gson", "com.EreliaStudio.OneBlockIslands.libs.gson")
}
