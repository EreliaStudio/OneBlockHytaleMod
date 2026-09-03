rootProject.name = "OneBlockHytaleMod"

include(":oneblock")
include(":oneblock-islands")
include(":oneblock-achievement")

project(":oneblock").projectDir = file("mods/oneblock")
project(":oneblock-islands").projectDir = file("mods/oneblock-islands")
project(":oneblock-achievement").projectDir = file("mods/oneblock-achievement")
