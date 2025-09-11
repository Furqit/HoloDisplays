package dev.furq.holodisplays.config

import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import java.nio.file.Path

object ConfigManager {
    private val configs = listOf(
        HologramConfig,
        DisplayConfig,
        AnimationConfig,
    )

    fun init(configDir: Path) = configs.forEach { config ->
        safeCall {
            config.init(configDir)
        }
    }

    fun reload() = configs.forEach { config ->
        safeCall {
            config.reload()
        }
    }
}