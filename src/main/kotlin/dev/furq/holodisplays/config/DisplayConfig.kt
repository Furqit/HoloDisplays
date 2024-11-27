package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object DisplayConfig : Config {
    private lateinit var displaysDir: Path
    private val displays = mutableMapOf<String, DisplayData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(configDir: Path) {
        displaysDir = configDir.resolve("displays").also {
            if (!it.exists()) it.createDirectories()
        }
        loadDisplays()
    }

    private fun parseDisplayData(json: JsonReader): DisplayData = json.run {
        beginObject()
        var displayType: DisplayData.DisplayType? = null

        while (hasNext()) {
            when (nextName()) {
                "type" -> {
                    displayType = when (nextString().lowercase()) {
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

        DisplayData(
            displayType ?: throw IllegalArgumentException("Display type is required")
        )
    }

    private fun JsonReader.parseTextDisplay(): DisplayData.DisplayType.Text {
        var lines = mutableListOf<String>()
        var rotation: HologramData.Rotation? = null
        var lineWidth: Int? = null
        var backgroundColor: String? = null
        var textOpacity: Int? = null
        var shadow: Boolean? = null
        var seeThrough: Boolean? = null
        var alignment: DisplayData.TextAlignment? = null
        var scale: Float? = null
        var billboardMode: BillboardMode? = null

        while (hasNext()) {
            when (nextName()) {
                "lines" -> lines = parseStringArray().toMutableList()
                "rotation" -> rotation = parseRotation()
                "lineWidth" -> lineWidth = nextInt()
                "backgroundColor" -> backgroundColor = nextString()
                "textOpacity" -> textOpacity = nextInt()
                "shadow" -> shadow = nextBoolean()
                "seeThrough" -> seeThrough = nextBoolean()
                "alignment" -> alignment = DisplayData.TextAlignment.valueOf(nextString().uppercase())
                "scale" -> scale = nextDouble().toFloat()
                "billboardMode" -> billboardMode = BillboardMode.valueOf(nextString().uppercase())
                else -> skipValue()
            }
        }

        return DisplayData.DisplayType.Text(
            lines = lines,
            rotation = rotation,
            lineWidth = lineWidth,
            backgroundColor = backgroundColor,
            textOpacity = textOpacity,
            shadow = shadow,
            seeThrough = seeThrough,
            alignment = alignment,
            scale = scale,
            billboardMode = billboardMode
        )
    }

    private fun JsonReader.parseItemDisplay(): DisplayData.DisplayType.Item {
        var id = ""
        var rotation: HologramData.Rotation? = null
        var scale: Float? = null
        var billboardMode: BillboardMode? = null
        var displayType = "ground"

        while (hasNext()) {
            when (nextName()) {
                "id" -> id = nextString()
                "displayType" -> displayType = nextString().lowercase()
                "rotation" -> rotation = parseRotation()
                "scale" -> scale = nextDouble().toFloat()
                "billboardMode" -> billboardMode = BillboardMode.valueOf(nextString().uppercase())
                else -> skipValue()
            }
        }

        return DisplayData.DisplayType.Item(
            id = id,
            itemDisplayType = displayType,
            rotation = rotation,
            scale = scale,
            billboardMode = billboardMode
        )
    }

    private fun JsonReader.parseBlockDisplay(): DisplayData.DisplayType.Block {
        var id = ""
        var rotation: HologramData.Rotation? = null
        var scale: Float? = null
        var billboardMode: BillboardMode? = null

        while (hasNext()) {
            when (nextName()) {
                "id" -> id = nextString()
                "rotation" -> rotation = parseRotation()
                "scale" -> scale = nextDouble().toFloat()
                "billboardMode" -> billboardMode = BillboardMode.valueOf(nextString().uppercase())
                else -> skipValue()
            }
        }

        return DisplayData.DisplayType.Block(
            id = id,
            rotation = rotation,
            scale = scale,
            billboardMode = billboardMode
        )
    }

    private fun JsonReader.parseStringArray(): List<String> {
        val result = mutableListOf<String>()
        beginArray()
        while (hasNext()) {
            result.add(nextString())
        }
        endArray()
        return result.toList()
    }


    private fun JsonReader.parseRotation() = beginObject().run {
        var pitch = 0.0f
        var yaw = 0.0f

        while (hasNext()) {
            when (nextName()) {
                "pitch" -> pitch = nextDouble().toFloat()
                "yaw" -> yaw = nextDouble().toFloat()
                else -> skipValue()
            }
        }
        endObject()
        HologramData.Rotation(pitch, yaw)
    }

    private fun JsonWriter.writeDisplay(display: DisplayData) {
        beginObject()

        when (val type = display.displayType) {
            is DisplayData.DisplayType.Text -> {
                name("type").value("text")
                name("lines").beginArray()
                type.lines.forEach { value(it) }
                endArray()
                type.rotation?.let { rotation ->
                    name("rotation").beginObject()
                    name("pitch").value(rotation.pitch)
                    name("yaw").value(rotation.yaw)
                    endObject()
                }
                type.scale?.let { name("scale").value(it) }
                type.billboardMode?.let { name("billboardMode").value(it.name) }
                type.lineWidth?.let { name("lineWidth").value(it) }
                type.backgroundColor?.let { name("backgroundColor").value(it) }
                type.textOpacity?.let { name("textOpacity").value(it) }
                type.shadow?.let { name("shadow").value(it) }
                type.seeThrough?.let { name("seeThrough").value(it) }
                type.alignment?.let { name("alignment").value(it.name) }
            }

            is DisplayData.DisplayType.Item -> {
                name("type").value("item")
                name("id").value(type.id)
                name("displayType").value(type.itemDisplayType)
                type.rotation?.let { rotation ->
                    name("rotation").beginObject()
                    name("pitch").value(rotation.pitch)
                    name("yaw").value(rotation.yaw)
                    endObject()
                }
                type.scale?.let { name("scale").value(it) }
                type.billboardMode?.let { name("billboardMode").value(it.name) }
            }

            is DisplayData.DisplayType.Block -> {
                name("type").value("block")
                name("id").value(type.id)
                type.rotation?.let { rotation ->
                    name("rotation").beginObject()
                    name("pitch").value(rotation.pitch)
                    name("yaw").value(rotation.yaw)
                    endObject()
                }
                type.scale?.let { name("scale").value(it) }
                type.billboardMode?.let { name("billboardMode").value(it.name) }
            }
        }

        endObject()
    }

    private fun loadDisplays() {
        displays.clear()
        runCatching {
            displaysDir.toFile().listFiles(jsonFilter)?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    displays[file.nameWithoutExtension] = parseDisplayData(json)
                }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to load displays", it) }
    }

    override fun reload() {
        loadDisplays()
    }

    fun getDisplay(name: String): DisplayData? = displays[name]
    fun getDisplays(): Map<String, DisplayData> = displays.toMap()
    fun exists(name: String): Boolean = displays.containsKey(name)

    fun saveDisplay(name: String, display: DisplayData) {
        displays[name] = display

        runCatching {
            val file = displaysDir.resolve("$name.json").toFile()
            file.parentFile.mkdirs()
            
            file.outputStream().writer().use { writer ->
                JsonWriter.json(writer).use { json -> json.writeDisplay(display) }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to save display $name", it) }
    }

    fun deleteDisplay(name: String) {
        runCatching {
            displaysDir.resolve("$name.json").toFile().let {
                if (it.exists()) it.delete()
            }
            displays.remove(name)
        }.onFailure { HoloDisplays.LOGGER.error("Failed to delete display $name", it) }
    }
}