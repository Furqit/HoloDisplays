package dev.furq.holodisplays.config

import java.nio.file.Path

interface Config {
    fun init(configDir: Path)
    fun reload()
}