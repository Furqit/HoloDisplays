pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.+"
}

stonecutter {
    create(rootProject) {
        versions("1.20.6", "1.21.3", "1.21.4")
        vcsVersion = "1.20.6"
    }
}

rootProject.name = "HoloDisplays"