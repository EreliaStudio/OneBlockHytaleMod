rootProject.name = "OneBlockMods"

include(":oneblock")
project(":oneblock").projectDir = file("mods/oneblock")

include(":oneblock-world")
project(":oneblock-world").projectDir = file("mods/oneblock-world")

include(":oneblock-salvager")
project(":oneblock-salvager").projectDir = file("mods/oneblock-salvager")
