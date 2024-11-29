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
        var position = HologramData.Position()
        var rotation = HologramData.Rotation()
        var scale = HologramData.Scale()
        var billboardMode = BillboardMode.CENTER
        var updateRate = 20
        var viewRange = 16.0

        while (hasNext()) {
            when (nextName()) {
                "displays" -> displays = parseDisplayLines()
                "position" -> position = parsePosition()
                "rotation" -> rotation = parseRotationArray()
                "scale" -> scale = parseScaleArray()
                "billboardMode" -> billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "updateRate" -> updateRate = nextInt()
                "viewRange" -> viewRange = nextDouble()
                else -> skipValue()
            }
        }
        endObject()

        HologramData(displays, position, scale, billboardMode, updateRate, viewRange, rotation)
    }

    private fun JsonReader.parseDisplayLines(): MutableList<HologramData.DisplayLine> {
        val lines = mutableListOf<HologramData.DisplayLine>()
        beginArray()
        while (hasNext()) {
            beginObject()
            var name = ""
            var offset = HologramData.Offset()

            while (hasNext()) {
                when (nextName()) {
                    "name" -> name = nextString()
                    "offset" -> offset = parseOffsetArray()
                    else -> skipValue()
                }
            }
            endObject()

            if (name.isNotEmpty()) {
                lines.add(HologramData.DisplayLine(name, offset))
            }
        }
        endArray()
        return lines
    }

    private fun JsonReader.parseOffsetArray(): HologramData.Offset {
        beginArray()
        val x = nextDouble().toFloat()
        val y = nextDouble().toFloat()
        val z = nextDouble().toFloat()
        endArray()
        return HologramData.Offset(x, y, z)
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

    private fun JsonReader.parseRotationArray(): HologramData.Rotation {
        beginArray()
        val pitch = nextDouble().toFloat()
        val yaw = nextDouble().toFloat()
        val roll = nextDouble().toFloat()
        endArray()
        return HologramData.Rotation(pitch, yaw, roll)
    }

    private fun JsonReader.parseScaleArray(): HologramData.Scale {
        beginArray()
        val x = nextDouble().toFloat()
        val y = nextDouble().toFloat()
        val z = nextDouble().toFloat()
        endArray()
        return HologramData.Scale(x, y, z)
    }

    private fun writeHologram(json: JsonWriter, hologram: HologramData) = json.run {
        beginObject()

        name("displays").beginArray()
        hologram.displays.forEach { line ->
            beginObject()
            name("name").value(line.displayId)
            name("offset").beginArray()
            value(line.offset.x)
            value(line.offset.y)
            value(line.offset.z)
            endArray()
            endObject()
        }
        endArray()

        name("position").beginObject()
        name("world").value(hologram.position.world)
        name("x").value(hologram.position.x)
        name("y").value(hologram.position.y)
        name("z").value(hologram.position.z)
        endObject()

        name("rotation").beginArray()
        value(hologram.rotation.pitch)
        value(hologram.rotation.yaw)
        value(hologram.rotation.roll)
        endArray()

        name("scale").beginArray()
        value(hologram.scale.x)
        value(hologram.scale.y)
        value(hologram.scale.z)
        endArray()

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

        val file = hologramsDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream()
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