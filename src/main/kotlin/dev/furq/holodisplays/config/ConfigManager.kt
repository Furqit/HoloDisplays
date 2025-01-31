package dev.furq.holodisplays.config

import dev.furq.holodisplays.handlers.ErrorHandler
import java.nio.file.Path

object ConfigManager {
    private val configs = listOf(
        HologramConfig,
        DisplayConfig,
        AnimationConfig,
    )

    fun init(configDir: Path) = configs.forEach { config ->
        ErrorHandler.withCatch {
            config.init(configDir)
        }
    }

    fun reload() = configs.forEach { config ->
        ErrorHandler.withCatch {
            config.reload()
        }
    }
}