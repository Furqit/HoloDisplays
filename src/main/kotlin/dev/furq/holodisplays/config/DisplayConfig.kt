package dev.furq.holodisplays.config

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import kotlinx.serialization.json.*
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object DisplayConfig : Config {
    override lateinit var configDir: Path
    private val displays = ConcurrentHashMap<String, DisplayData>()
    private val json = sharedJson

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("displays")
        super.init(baseDir)
    }

    override fun reload() {
        val files = configDir.toFile().listFiles { it.extension == "json" }
            ?: throw ConfigException("Failed to list display config files")
        val loaded = mutableMapOf<String, DisplayData>()
        files.forEach { file ->
            safeCall {
                val jsonContent = file.readText()
                val displayData = deserializeDisplayData(jsonContent)
                loaded[file.nameWithoutExtension] = displayData
            }
        }
        displays.clear()
        displays.putAll(loaded)
    }

    fun getDisplay(name: String): DisplayData? = displays[name]
    fun getDisplayOrAPI(name: String): DisplayData? = displays[name] ?: HoloDisplaysAPIInternal.getDisplayUnchecked(name)
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
            "text" -> json.decodeFromJsonElement<TextDisplay>(jsonElement)
            "item" -> json.decodeFromJsonElement<ItemDisplay>(jsonElement)
            "block" -> json.decodeFromJsonElement<BlockDisplay>(jsonElement)
            "entity" -> json.decodeFromJsonElement<EntityDisplay>(jsonElement)
            else -> throw ConfigException("Unknown display type: $type")
        }

        return DisplayData(display)
    }

    private fun serializeDisplayData(displayData: DisplayData): String {
        val (displayElement, typeName) = when (val display = displayData.type) {
            is TextDisplay -> json.encodeToJsonElement(TextDisplay.serializer(), display) to "text"
            is ItemDisplay -> json.encodeToJsonElement(ItemDisplay.serializer(), display) to "item"
            is BlockDisplay -> json.encodeToJsonElement(BlockDisplay.serializer(), display) to "block"
            is EntityDisplay -> json.encodeToJsonElement(EntityDisplay.serializer(), display) to "entity"
            else -> throw ConfigException("Unknown display type: ${display::class.simpleName}")
        }

        return json.encodeToString(
            JsonObject(displayElement.jsonObject + ("type" to JsonPrimitive(typeName)))
        )
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