package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.BaseDisplay
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path

object DisplayConfig : Config {
    override lateinit var configDir: Path
    private val displays = mutableMapOf<String, DisplayData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("displays")
        super.init(baseDir)
    }

    override fun reload() {
        displays.clear()
        runCatching {
            configDir.toFile().listFiles(jsonFilter)?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    displays[file.nameWithoutExtension] = parseDisplayData(json)
                }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to load displays", it) }
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
                "lines" -> builder.lines = parseStringArray().toMutableList()
                "rotation" -> builder.rotation = parseRotationArray()
                "scale" -> builder.scale = parseScaleArray()
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
                "rotation" -> builder.rotation = parseRotationArray()
                "scale" -> builder.scale = parseScaleArray()
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
                "rotation" -> builder.rotation = parseRotationArray()
                "scale" -> builder.scale = parseScaleArray()
                "billboardMode" -> builder.billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "conditionalPlaceholder" -> builder.conditionalPlaceholder = nextString()
                else -> skipValue()
            }
        }

        return DisplayData(builder.build())
    }

    private fun JsonReader.parseRotationArray(): Vector3f {
        beginArray()
        val pitch = nextDouble().toFloat()
        val yaw = nextDouble().toFloat()
        val roll = nextDouble().toFloat()
        endArray()
        return Vector3f(pitch, yaw, roll)
    }

    private fun JsonReader.parseScaleArray(): Vector3f {
        beginArray()
        val x = nextDouble().toFloat()
        val y = nextDouble().toFloat()
        val z = nextDouble().toFloat()
        endArray()
        return Vector3f(x, y, z)
    }

    private fun JsonReader.parseStringArray(): List<String> {
        val result = mutableListOf<String>()
        beginArray()
        while (hasNext()) {
            result.add(nextString())
        }
        endArray()
        return result
    }

    fun getDisplay(name: String): DisplayData? = displays[name]
    fun getDisplays(): Map<String, DisplayData> = displays.toMap()
    fun exists(name: String): Boolean = displays.containsKey(name)

    fun saveDisplay(name: String, display: DisplayData) {
        displays[name] = display

        runCatching {
            val file = configDir.resolve("$name.json").toFile()
            file.parentFile.mkdirs()

            file.outputStream().writer().use { writer ->
                JsonWriter.json(writer).use { json -> writeDisplay(json, display) }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to save display $name", it) }
    }

    private fun writeDisplay(json: JsonWriter, displayData: DisplayData) {
        json.beginObject()

        when (val display = displayData.display) {
            is TextDisplay -> {
                json.name("type").value("text")
                json.name("lines").beginArray()
                display.lines.forEach { json.value(it) }
                json.endArray()
                display.lineWidth?.let { json.name("lineWidth").value(it) }
                display.backgroundColor?.let { json.name("backgroundColor").value(it) }
                display.textOpacity?.let { json.name("textOpacity").value(it) }
                display.shadow?.let { json.name("shadow").value(it) }
                display.seeThrough?.let { json.name("seeThrough").value(it) }
                display.alignment?.let { json.name("alignment").value(it.name) }
                writeCommonProperties(json, display)
            }

            is ItemDisplay -> {
                json.name("type").value("item")
                json.name("id").value(display.id)
                json.name("displayType").value(display.itemDisplayType)
                display.customModelData?.let { json.name("customModelData").value(it) }
                writeCommonProperties(json, display)
            }

            is BlockDisplay -> {
                json.name("type").value("block")
                json.name("id").value(display.id)
                writeCommonProperties(json, display)
            }
        }

        json.endObject()
    }

    private fun writeCommonProperties(json: JsonWriter, display: BaseDisplay) {
        display.rotation?.let { rotation ->
            json.name("rotation").beginArray()
            json.value(rotation.x)
            json.value(rotation.y)
            json.value(rotation.z)
            json.endArray()
        }

        display.scale?.let { scale ->
            json.name("scale").beginArray()
            json.value(scale.x)
            json.value(scale.y)
            json.value(scale.z)
            json.endArray()
        }

        display.billboardMode?.let { json.name("billboardMode").value(it.name) }
        display.conditionalPlaceholder?.let { json.name("conditionalPlaceholder").value(it) }
    }

    fun deleteDisplay(name: String) {
        runCatching {
            configDir.resolve("$name.json").toFile().let {
                if (it.exists()) it.delete()
            }
            displays.remove(name)
        }.onFailure { HoloDisplays.LOGGER.error("Failed to delete display $name", it) }
    }
}