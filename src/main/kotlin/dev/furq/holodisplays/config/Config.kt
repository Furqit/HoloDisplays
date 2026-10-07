package dev.furq.holodisplays.config

import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@OptIn(ExperimentalSerializationApi::class)
internal val sharedJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
    allowTrailingComma = true
    allowComments = true
}

interface Config {
    val configDir: Path

    fun init(baseDir: Path) = safeCall {
        if (!configDir.exists()) {
            configDir.createDirectories()
        }
        reload()
    }

    fun reload()
}