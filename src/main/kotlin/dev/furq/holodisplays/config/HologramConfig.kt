package dev.furq.holodisplays.config

import dev.furq.holodisplays.api.HoloDisplaysAPI
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.nio.file.Path

object HologramConfig : Config {
    override lateinit var configDir: Path
    private val holograms = mutableMapOf<String, HologramData>()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        allowTrailingComma = true
        allowComments = true
    }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("holograms")
        super.init(baseDir)
    }

    override fun reload() {
        holograms.clear()
        configDir.toFile().listFiles { it.extension == "json" }
            ?.forEach { file ->
                val jsonContent = file.readText()
                val hologramData = json.decodeFromString<HologramData>(jsonContent)
                holograms[file.nameWithoutExtension] = hologramData
            }
            ?: throw ConfigException("Failed to list hologram config files")
    }


    fun getHologram(name: String): HologramData? = holograms[name]
    fun getHologramOrAPI(name: String): HologramData? = holograms[name] ?: HoloDisplaysAPI.get().getHologram(name)
    fun getHolograms(): Map<String, HologramData> = holograms
    fun exists(name: String): Boolean = holograms.containsKey(name)

    fun saveHologram(name: String, hologram: HologramData) = safeCall {
        holograms[name] = hologram
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        val jsonContent = json.encodeToString(hologram)
        file.writeText(jsonContent)
    }

    fun deleteHologram(name: String) = safeCall {
        val file = configDir.resolve("$name.json").toFile()
        if (!file.exists()) {
            throw ConfigException("Hologram config file for $name does not exist")
        }
        if (!file.delete()) {
            throw ConfigException("Failed to delete hologram config file for $name")
        }
        holograms.remove(name)
    }
}