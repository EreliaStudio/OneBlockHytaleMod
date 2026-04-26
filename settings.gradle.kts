rootProject.name = "OneBlockHytaleMod"

include(
    ":oneblock-core",
    ":oneblock-progression",
    ":oneblock-salvager",
    ":oneblock-world"
)

project(":oneblock-core").projectDir = file("mods/oneblock-core")
project(":oneblock-progression").projectDir = file("mods/oneblock-progression")
project(":oneblock-salvager").projectDir = file("mods/oneblock-salvager")
project(":oneblock-world").projectDir = file("mods/oneblock-world")