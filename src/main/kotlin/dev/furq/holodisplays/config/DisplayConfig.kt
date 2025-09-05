package dev.furq.holodisplays.config

import dev.furq.holodisplays.api.HoloDisplaysAPI
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.BaseDisplay
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.nio.file.Path

object DisplayConfig : Config {
    override lateinit var configDir: Path
    private val displays = mutableMapOf<String, DisplayData>()

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("displays")
        super.init(baseDir)
    }

    override fun reload() = ErrorHandler.withCatch {
        displays.clear()
        configDir.toFile().listFiles(JsonUtils.jsonFilter)
            ?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    displays[file.nameWithoutExtension] = parseDisplayData(json)
                }
            }
            ?: throw ConfigException("Failed to list display config files")
    }

    private fun parseDisplayData(json: JsonReader): DisplayData = json.run {
        beginObject()
        lateinit var displayData: DisplayData

        while (hasNext()) {
            when (nextName()) {
                "type" -> {
                    displayData = when (nextString().lowercase()) {
                        "text" -> parseTextDisplay()
                        "item" -> parseItemDisplay()
                        "block" -> parseBlockDisplay()
                        else -> throw IllegalArgumentException("Invalid display type")
                    }
                }

                else -> skipValue()
            }
        }
        endObject()

        displayData
    }

    private fun JsonReader.parseTextDisplay(): DisplayData {
        val builder = TextDisplay.Builder()

        while (hasNext()) {
            when (nextName()) {
                "lines" -> builder.lines = JsonUtils.parseStringList(this).toMutableList()
                "rotation" -> builder.rotation = JsonUtils.parseVector3f(this)
                "scale" -> builder.scale = JsonUtils.parseVector3f(this)
                "lineWidth" -> builder.lineWidth = nextInt()
                "backgroundColor" -> builder.backgroundColor = nextString()
                "textOpacity" -> builder.textOpacity = nextInt()
                "shadow" -> builder.shadow = nextBoolean()
                "seeThrough" -> builder.seeThrough = nextBoolean()
                "alignment" -> builder.alignment = TextDisplay.TextAlignment.valueOf(nextString().uppercase())
                "billboardMode" -> builder.billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "conditionalPlaceholder" -> builder.conditionalPlaceholder = nextString()
                else -> skipValue()
            }
        }

        return DisplayData(builder.build())
    }

    private fun JsonReader.parseItemDisplay(): DisplayData {
        val builder = ItemDisplay.Builder()

        while (hasNext()) {
            when (nextName()) {
                "id" -> builder.id = nextString()
                "displayType" -> builder.itemDisplayType = nextString().lowercase()
                "rotation" -> builder.rotation = JsonUtils.parseVector3f(this)
                "scale" -> builder.scale = JsonUtils.parseVector3f(this)
                "billboardMode" -> builder.billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "customModelData" -> builder.customModelData = nextInt()
                "conditionalPlaceholder" -> builder.conditionalPlaceholder = nextString()
                else -> skipValue()
            }
        }

        return DisplayData(builder.build())
    }

    private fun JsonReader.parseBlockDisplay(): DisplayData {
        val builder = BlockDisplay.Builder()

        while (hasNext()) {
            when (nextName()) {
                "id" -> builder.id = nextString()
                "rotation" -> builder.rotation = JsonUtils.parseVector3f(this)
                "scale" -> builder.scale = JsonUtils.parseVector3f(this)
                "billboardMode" -> builder.billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "conditionalPlaceholder" -> builder.conditionalPlaceholder = nextString()
                else -> skipValue()
            }
        }

        return DisplayData(builder.build())
    }

    fun getDisplay(name: String): DisplayData? = displays[name]
    fun getDisplays(): Map<String, DisplayData> = displays.toMap()
    fun exists(name: String): Boolean = displays.containsKey(name)

    fun saveDisplay(name: String, display: DisplayData) = ErrorHandler.withCatch {
        displays[name] = display
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream().writer().use { writer ->
            JsonWriter.json(writer).use { json -> writeDisplay(json, display) }
        }
    }

    private fun writeDisplay(json: JsonWriter, displayData: DisplayData) = json.run {
        beginObject()

        when (val display = displayData.display) {
            is TextDisplay -> {
                name("type").value("text")
                JsonUtils.writeStringList(this, "lines", display.lines)
                display.lineWidth?.let { name("lineWidth").value(it) }
                display.backgroundColor?.let { name("backgroundColor").value(it) }
                display.textOpacity?.let { name("textOpacity").value(it) }
                display.shadow?.let { name("shadow").value(it) }
                display.seeThrough?.let { name("seeThrough").value(it) }
                display.alignment?.let { name("alignment").value(it.name) }
                writeCommonProperties(this, display)
            }

            is ItemDisplay -> {
                name("type").value("item")
                name("id").value(display.id)
                name("displayType").value(display.itemDisplayType)
                display.customModelData?.let { name("customModelData").value(it) }
                writeCommonProperties(this, display)
            }

            is BlockDisplay -> {
                name("type").value("block")
                name("id").value(display.id)
                writeCommonProperties(this, display)
            }
        }

        endObject()
    }

    private fun writeCommonProperties(json: JsonWriter, display: BaseDisplay) {
        display.rotation?.let { JsonUtils.writeVector3f(json, "rotation", it) }
        display.scale?.let { JsonUtils.writeVector3f(json, "scale", it) }
        display.billboardMode?.let { json.name("billboardMode").value(it.name) }
        display.conditionalPlaceholder?.let { json.name("conditionalPlaceholder").value(it) }
    }

    fun deleteDisplay(name: String) = ErrorHandler.withCatch {
        val file = configDir.resolve("$name.json").toFile()
        if (!file.exists()) {
            throw ConfigException("Display config file for $name does not exist")
        }
        if (!file.delete()) {
            throw ConfigException("Failed to delete display config file for $name")
        }
        displays.remove(name)
    }

    fun getDisplayOrAPI(id: String): DisplayData? {
        return displays[id] ?: HoloDisplaysAPI.get().getDisplay(id)
    }
}