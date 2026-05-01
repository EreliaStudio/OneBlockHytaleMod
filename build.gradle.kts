import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("com.gradleup.shadow") version "9.2.2" apply false
}

group = "com.EreliaStudio"
version = "1.0.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        add("compileOnly", files(rootProject.file("libs/HytaleServer.jar")))

        add("implementation", "com.google.guava:guava:32.1.3-jre")
        add("implementation", "com.google.code.gson:gson:2.10.1")

        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.10.1")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        relocate("com.google.gson", "com.EreliaStudio.OneBlock.libs.gson")
    }

    tasks.named("jar") {
        enabled = false
    }

    tasks.named("assemble") {
        dependsOn(tasks.named("shadowJar"))
    }

    val deploy by tasks.registering {
        group = "distribution"
        description = "Copies the shaded jar and asset pack into hytale-server/mods"
        dependsOn(tasks.named("shadowJar"), tasks.named("processResources"))

        val jarFile = tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
        val resourcesDir = tasks.named<ProcessResources>("processResources").map { it.destinationDir }
        val modsDir = rootProject.file("hytale-server/mods")
        val packId = "${rootProject.group}_${project.name}"

        doFirst {
            println("Deploying JAR: ${jarFile.get().asFile} -> $modsDir")
            println("Deploying asset pack: ${resourcesDir.get()} -> ${modsDir}/$packId")
        }

        doLast {
            // Copy JAR
            copy {
                from(jarFile)
                into(modsDir)
            }
            // Copy asset pack resources into com.EreliaStudio_oneblock/
            copy {
                from(resourcesDir)
                into(file("$modsDir/$packId"))
            }
        }
    }

    tasks.register("buildAndDeploy") {
        group = "distribution"
        description = "Builds this mod and deploys it to hytale-server/mods"
        dependsOn(tasks.named("build"))
        dependsOn(deploy)
        deploy.get().mustRunAfter(tasks.named("build"))
    }

    tasks.named<ProcessResources>("processResources") {
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
    description = "Builds all mods."
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