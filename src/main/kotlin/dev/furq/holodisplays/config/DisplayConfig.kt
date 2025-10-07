package dev.furq.holodisplays.config

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.*
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

object DisplayConfig : Config {
    override lateinit var configDir: Path
    private val displays = mutableMapOf<String, DisplayData>()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        allowTrailingComma = true
        allowComments = true
    }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("displays")
        super.init(baseDir)
    }

    override fun reload() {
        displays.clear()
        configDir.toFile().listFiles { it.extension == "json" }
            ?.forEach { file ->
                val jsonContent = file.readText()
                val displayData = deserializeDisplayData(jsonContent)
                displays[file.nameWithoutExtension] = displayData
            }
            ?: throw ConfigException("Failed to list display config files")
    }


    fun getDisplay(name: String): DisplayData? = displays[name]
    fun getDisplayOrAPI(name: String): DisplayData? = displays[name] ?: HoloDisplaysAPIInternal.getDisplay(name)
    fun getDisplays(): Map<String, DisplayData> = displays
    fun exists(name: String): Boolean = displays.containsKey(name)

    fun saveDisplay(name: String, display: DisplayData) = safeCall {
        displays[name] = display
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        val jsonContent = serializeDisplayData(display)
        file.writeText(jsonContent)
    }

    private fun deserializeDisplayData(jsonContent: String): DisplayData {
        val jsonElement = json.parseToJsonElement(jsonContent)
        val type = jsonElement.jsonObject["type"]?.jsonPrimitive?.content ?: throw ConfigException("Display config missing 'type' field")

        val display = when (type.lowercase()) {
            "text" -> json.decodeFromString<TextDisplay>(jsonContent)
            "item" -> json.decodeFromString<ItemDisplay>(jsonContent)
            "block" -> json.decodeFromString<BlockDisplay>(jsonContent)
            "entity" -> json.decodeFromString<EntityDisplay>(jsonContent)
            else -> throw ConfigException("Unknown display type: $type")
        }

        return DisplayData(display)
    }

    private fun serializeDisplayData(displayData: DisplayData): String {
        val displayJson = when (val display = displayData.type) {
            is TextDisplay -> json.encodeToString(display)
            is ItemDisplay -> json.encodeToString(display)
            is BlockDisplay -> json.encodeToString(display)
            is EntityDisplay -> json.encodeToString(display)
            else -> throw ConfigException("Unknown display type: ${display::class.simpleName}")
        }

        return json.encodeToString(json.parseToJsonElement(displayJson))
    }

    fun deleteDisplay(name: String) = safeCall {
        val file = configDir.resolve("$name.json").toFile()
        if (!file.exists()) {
            throw ConfigException("Display config file for $name does not exist")
        }
        if (!file.delete()) {
            throw ConfigException("Failed to delete display config file for $name")
        }
        displays.remove(name)
    }
}