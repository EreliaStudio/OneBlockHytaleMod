import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "9.2.2" apply false
}

group = "com.EreliaStudio"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        compileOnly(files(rootProject.file("libs/HytaleServer.jar")))

        implementation("com.google.guava:guava:32.1.3-jre")
        implementation("com.google.code.gson:gson:2.10.1")

        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("") // shaded jar becomes the main artifact name
        relocate("com.google.gson", "com.EreliaStudio.OneBlock.libs.gson")
    }

    // Disable plain jar so you only produce the shaded jar
    tasks.jar { enabled = false }

    // Ensure normal build/assemble produces the shaded jar
    tasks.assemble {
        dependsOn(tasks.named("shadowJar"))
    }

    // --- Deploy step (copy shaded jar to server plugins folder) ---
    val deploy by tasks.registering(Copy::class) {
        group = "distribution"
        description = "Copies the shaded jar into hytale-server/mods"
        dependsOn(tasks.named("shadowJar"))

        val jarFile = tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
        from(jarFile)
        into(rootProject.file("hytale-server/mods"))

        doFirst {
            println("Deploying: ${jarFile.get().asFile} -> ${rootProject.file("hytale-server/mods")}")
        }
    }

    // --- One-command build + deploy (deploy runs after build) ---
    tasks.register("buildAndDeploy") {
        group = "distribution"
        description = "Builds the project (incl. shaded jar) and deploys it to the server plugins folder"
        dependsOn(tasks.build)
        dependsOn(deploy)
        deploy.get().mustRunAfter(tasks.build)
    }

    // Resource expansion kept as-is
    tasks.processResources {
        filesMatching("manifest.json") {
            expand(
                "version" to project.version,
                "name" to project.name
            )
        }
    }
}

tasks.register("buildAll") {
    group = "distribution"
    description = "Builds all mods (shadow jars)."
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("deployAll") {
    group = "distribution"
    description = "Deploys all mods to hytale-server/mods."
    dependsOn(subprojects.map { it.tasks.named("deploy") })
}

tasks.register("buildAndDeployAll") {
    group = "distribution"
    description = "Builds and deploys all mods."
    dependsOn(subprojects.map { it.tasks.named("buildAndDeploy") })
}
