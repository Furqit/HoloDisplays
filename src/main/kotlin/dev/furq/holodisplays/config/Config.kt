package dev.furq.holodisplays.config

import dev.furq.holodisplays.handlers.ErrorHandler
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

interface Config {
    val configDir: Path

    fun init(baseDir: Path) = ErrorHandler.withCatch {
        if (!configDir.exists()) {
            configDir.createDirectories()
        }
        reload()
    }

    fun reload()
}