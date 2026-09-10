base {
    archivesName.set("OneBlock")
}

dependencies {
    testImplementation(files(rootProject.file("libs/HytaleServer.jar")))
}

tasks.test {
    // Native inventory/recipe codecs initialize HytaleLogger before Gradle's test worker.
    jvmArgs("-Xbootclasspath/a:${rootProject.file("libs/HytaleServer.jar").absolutePath}")
    systemProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager")
}

val verifyExpeditionCatalog by tasks.registering(Exec::class) {
    group = "verification"
    description = "Checks the shared expedition catalogue, generated runtime, icons and UI resources."
    workingDir(rootProject.projectDir)
    environment("PYTHONDONTWRITEBYTECODE", "1")
    commandLine(providers.gradleProperty("pythonExecutable").getOrElse("python"),
        "-m", "unittest", "discover", "-s", "tools", "-p", "test_expedition_catalog.py")
}

val verifyExpeditionWiki by tasks.registering(Exec::class) {
    group = "verification"
    description = "Checks that the expedition wiki matches expeditions.json."
    workingDir(rootProject.projectDir)
    environment("PYTHONDONTWRITEBYTECODE", "1")
    commandLine(providers.gradleProperty("pythonExecutable").getOrElse("python"), "tools/generate_wiki.py", "--check")
}

tasks.check { dependsOn(verifyExpeditionCatalog, verifyExpeditionWiki) }
