rootProject.name = "OneBlockMods"

include(":oneblock-core")
project(":oneblock-core").projectDir = file("mods/oneblock-core")

include(":oneblock-progression")
project(":oneblock-progression").projectDir = file("mods/oneblock-progression")

include(":oneblock-salvager")
project(":oneblock-salvager").projectDir = file("mods/oneblock-salvager")

include(":oneblock-world")
project(":oneblock-world").projectDir = file("mods/oneblock-world")
