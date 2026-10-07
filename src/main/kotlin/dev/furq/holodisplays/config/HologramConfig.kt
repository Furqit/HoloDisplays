package dev.furq.holodisplays.config

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object HologramConfig : Config {
    override lateinit var configDir: Path
    private val holograms = ConcurrentHashMap<String, HologramData>()
    private val json = sharedJson

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("holograms")
        super.init(baseDir)
    }

    override fun reload() {
        val files = configDir.toFile().listFiles { it.extension == "json" }
            ?: throw ConfigException("Failed to list hologram config files")
        val loaded = mutableMapOf<String, HologramData>()
        files.forEach { file ->
            safeCall {
                val jsonContent = file.readText()
                val hologramData = json.decodeFromString<HologramData>(jsonContent)
                loaded[file.nameWithoutExtension] = hologramData
            }
        }
        holograms.clear()
        holograms.putAll(loaded)
    }


    fun getHologram(name: String): HologramData? = holograms[name]
    fun getHologramOrAPI(name: String): HologramData? = holograms[name] ?: HoloDisplaysAPIInternal.getHologramUnchecked(name)
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