rootProject.name = "OneBlockHytaleMod"

include(
    ":oneblock-core",
    ":oneblock-progression",
    ":oneblock-world"
)

project(":oneblock-core").projectDir = file("mods/oneblock-core")
project(":oneblock-progression").projectDir = file("mods/oneblock-progression")
project(":oneblock-world").projectDir = file("mods/oneblock-world")