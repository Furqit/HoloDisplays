package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import java.nio.file.Path

object ConfigManager {
    private val configs = listOf(
        HologramConfig,
        DisplayConfig,
        AnimationConfig,
    )

    fun init(configDir: Path) = configs.forEach { config ->
        runCatching {
            config.init(configDir)
        }.onFailure { error ->
            HoloDisplays.LOGGER.error("Failed to initialize ${config::class.simpleName}", error)
        }
    }

    fun reload() = configs.forEach { config ->
        runCatching {
            config.reload()
        }.onFailure { error ->
            HoloDisplays.LOGGER.error("Failed to reload ${config::class.simpleName}", error)
        }
    }
}