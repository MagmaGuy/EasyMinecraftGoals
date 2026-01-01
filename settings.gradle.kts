pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "EasyMinecraftGoals"

include("core")
include("v1_19_R3")
include("v1_20_R1")
include("v1_20_R2")
include("v1_20_R3")
include("v1_20_R4")
include("v1_21_R1")
include("v1_21_R2")
include("v1_21_R3")
include("v1_21_R4")
include("v1_21_R5")
include("v1_21_R6")
include("v1_21_R7_common")
include("v1_21_R7_spigot")
include("v1_21_R7_paper")
include("dist")

project(":core").projectDir = file("EasyMinecraftGoals/core")
project(":v1_19_R3").projectDir = file("EasyMinecraftGoals/v1_19_R3")
project(":v1_20_R1").projectDir = file("EasyMinecraftGoals/v1_20_R1")
project(":v1_20_R2").projectDir = file("EasyMinecraftGoals/v1_20_R2")
project(":v1_20_R3").projectDir = file("EasyMinecraftGoals/v1_20_R3")
project(":v1_20_R4").projectDir = file("EasyMinecraftGoals/v1_20_R4")
project(":v1_21_R1").projectDir = file("EasyMinecraftGoals/v1_21_R1")
project(":v1_21_R2").projectDir = file("EasyMinecraftGoals/v1_21_R2")
project(":v1_21_R3").projectDir = file("EasyMinecraftGoals/v1_21_R3")
project(":v1_21_R4").projectDir = file("EasyMinecraftGoals/v1_21_R4")
project(":v1_21_R5").projectDir = file("EasyMinecraftGoals/v1_21_R5")
project(":v1_21_R6").projectDir = file("EasyMinecraftGoals/v1_21_R6")
project(":v1_21_R7_common").projectDir = file("EasyMinecraftGoals/v1_21_R7_common")
project(":v1_21_R7_spigot").projectDir = file("EasyMinecraftGoals/v1_21_R7_spigot")
project(":v1_21_R7_paper").projectDir = file("EasyMinecraftGoals/v1_21_R7_paper")
project(":dist").projectDir = file("EasyMinecraftGoals/dist")
