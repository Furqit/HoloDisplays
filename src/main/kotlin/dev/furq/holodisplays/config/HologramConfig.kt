package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.HologramData
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object HologramConfig : Config {
    private lateinit var hologramsDir: Path
    private val holograms = mutableMapOf<String, HologramData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(configDir: Path) {
        hologramsDir = configDir.resolve("holograms").also { dir ->
            if (!dir.exists()) dir.createDirectories()
        }
        loadHolograms()
    }

    private fun loadHolograms() = runCatching {
        holograms.clear()
        hologramsDir.toFile()
            .listFiles(jsonFilter)
            ?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    holograms[file.nameWithoutExtension] = parseHologramData(json)
                }
            }
    }.onFailure {
        HoloDisplays.LOGGER.error("Failed to load holograms", it)
    }

    private fun parseHologramData(json: JsonReader): HologramData = json.run {
        beginObject()
        var displays = mutableListOf<HologramData.DisplayLine>()
        var position = HologramData.Position(x = 0.0f, y = 0.0f, z = 0.0f)
        var rotation = HologramData.Rotation(0.0f, 0.0f)
        var scale = 1f
        var billboardMode = BillboardMode.CENTER
        var updateRate = 20
        var viewRange = 16.0

        while (hasNext()) {
            when (nextName()) {
                "displays" -> displays = parseLines()
                "position" -> position = parsePosition()
                "rotation" -> rotation = parseRotation()
                "scale" -> scale = nextDouble().toFloat()
                "billboardMode" -> billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "updateRate" -> updateRate = nextInt()
                "viewRange" -> viewRange = nextDouble()
                else -> skipValue()
            }
        }
        endObject()

        HologramData(displays, position, scale, billboardMode, updateRate, viewRange, rotation)
    }

    private fun JsonReader.parseLines(): MutableList<HologramData.DisplayLine> {
        val lines = mutableListOf<HologramData.DisplayLine>()
        beginArray()
        while (hasNext()) {
            beginObject()
            var reference: String? = null
            var type = "text"

            while (hasNext()) {
                when (nextName()) {
                    "text" -> {
                        reference = nextString()
                        type = "text"
                    }

                    "item" -> {
                        reference = nextString()
                        type = "item"
                    }

                    "block" -> {
                        reference = nextString()
                        type = "block"
                    }

                    else -> skipValue()
                }
            }
            endObject()

            reference?.let {
                val line = when (type) {
                    "text" -> HologramData.DisplayLine(text = it)
                    "item" -> HologramData.DisplayLine(item = it)
                    "block" -> HologramData.DisplayLine(block = it)
                    else -> null
                }
                line?.let { lines.add(line) }
            }
        }
        endArray()
        return lines
    }

    private fun JsonReader.parsePosition() = beginObject().run {
        var world = "minecraft:world"
        var x = 0.0f
        var y = 0.0f
        var z = 0.0f

        while (hasNext()) {
            when (nextName()) {
                "world" -> world = nextString()
                "x" -> x = nextDouble().toFloat()
                "y" -> y = nextDouble().toFloat()
                "z" -> z = nextDouble().toFloat()
                else -> skipValue()
            }
        }
        endObject()
        HologramData.Position(world, x, y, z)
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

    private fun writeHologram(json: JsonWriter, hologram: HologramData) = json.run {
        beginObject()

        name("displays").beginArray()
        hologram.displays.forEach { entity ->
            beginObject()
            when {
                entity.text != null -> name("text").value(entity.text)
                entity.item != null -> name("item").value(entity.item)
                entity.block != null -> name("block").value(entity.block)
            }
            endObject()
        }
        endArray()

        name("position").beginObject()
        name("world").value(hologram.position.world)
        name("x").value(hologram.position.x)
        name("y").value(hologram.position.y)
        name("z").value(hologram.position.z)
        endObject()

        name("rotation").beginObject()
        name("pitch").value(hologram.rotation.pitch)
        name("yaw").value(hologram.rotation.yaw)
        endObject()

        name("scale").value(hologram.scale)
        name("billboardMode").value(hologram.billboardMode.name)
        name("updateRate").value(hologram.updateRate)
        name("viewRange").value(hologram.viewRange)
        endObject()
    }


    fun getHologram(name: String): HologramData? = holograms[name]
    fun getHolograms(): Map<String, HologramData> = holograms.toMap()
    fun exists(name: String): Boolean = holograms.containsKey(name)

    override fun reload() {
        loadHolograms()
    }

    fun saveHologram(name: String, hologram: HologramData) = runCatching {
        holograms[name] = hologram

        hologramsDir.resolve("$name.json").toFile()
            .outputStream()
            .writer()
            .use { writer ->
                JsonWriter.json(writer).use { json ->
                    writeHologram(json, hologram)
                }
            }
    }.onFailure {
        HoloDisplays.LOGGER.error("Failed to save hologram $name", it)
    }

    fun deleteHologram(name: String) = runCatching {
        hologramsDir.resolve("$name.json").toFile().let {
            if (it.exists()) it.delete()
        }
        holograms.remove(name)
    }.onFailure {
        HoloDisplays.LOGGER.error("Failed to delete hologram $name", it)
    }
}