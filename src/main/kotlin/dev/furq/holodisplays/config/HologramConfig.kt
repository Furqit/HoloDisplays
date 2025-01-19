package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.HologramData
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path

object HologramConfig : Config {
    override lateinit var configDir: Path
    private val holograms = mutableMapOf<String, HologramData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("holograms")
        super.init(baseDir)
    }

    override fun reload() {
        holograms.clear()
        runCatching {
            configDir.toFile()
                .listFiles(jsonFilter)
                ?.forEach { file ->
                    JsonReader.json5(file.inputStream().reader()).use { json ->
                        holograms[file.nameWithoutExtension] = parseHologramData(json)
                    }
                }
        }.onFailure {
            HoloDisplays.LOGGER.error("Failed to load holograms", it)
        }
    }

    private fun parseHologramData(json: JsonReader): HologramData = json.run {
        val builder = HologramData.Builder()
        beginObject()

        while (hasNext()) {
            when (nextName()) {
                "displays" -> builder.displays = parseDisplayLines()
                "position" -> {
                    val (world, position) = parsePosition()
                    builder.world = world
                    builder.position = position
                }

                "rotation" -> builder.rotation = parseRotationArray()
                "scale" -> builder.scale = parseScaleArray()
                "billboardMode" -> builder.billboardMode = BillboardMode.valueOf(nextString().uppercase())
                "updateRate" -> builder.updateRate = nextInt()
                "viewRange" -> builder.viewRange = nextDouble()
                "conditionalPlaceholder" -> builder.conditionalPlaceholder = nextString()
                else -> skipValue()
            }
        }
        endObject()

        builder.build()
    }

    private fun JsonReader.parseDisplayLines(): MutableList<HologramData.DisplayLine> {
        val lines = mutableListOf<HologramData.DisplayLine>()
        beginArray()
        while (hasNext()) {
            beginObject()
            var name = ""
            var offset = Vector3f()

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

    private fun JsonReader.parseOffsetArray(): Vector3f {
        beginArray()
        val x = nextDouble().toFloat()
        val y = nextDouble().toFloat()
        val z = nextDouble().toFloat()
        endArray()
        return Vector3f(x, y, z)
    }

    private fun JsonReader.parsePosition(): Pair<String, Vector3f> {
        var world = "minecraft:overworld"
        var x = 0f
        var y = 0f
        var z = 0f

        beginObject()
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
        return world to Vector3f(x, y, z)
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

    fun getHologram(name: String): HologramData? = holograms[name]
    fun getHolograms(): Map<String, HologramData> = holograms.toMap()
    fun exists(name: String): Boolean = holograms.containsKey(name)

    fun saveHologram(name: String, hologram: HologramData) = runCatching {
        holograms[name] = hologram

        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream().writer().use { writer ->
            JsonWriter.json(writer).use { json -> writeHologram(json, hologram) }
        }
    }.onFailure {
        HoloDisplays.LOGGER.error("Failed to save hologram $name", it)
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
        name("world").value(hologram.world)
        name("x").value(hologram.position.x)
        name("y").value(hologram.position.y)
        name("z").value(hologram.position.z)
        endObject()

        name("rotation").beginArray()
        value(hologram.rotation.x)
        value(hologram.rotation.y)
        value(hologram.rotation.z)
        endArray()

        name("scale").beginArray()
        value(hologram.scale.x)
        value(hologram.scale.y)
        value(hologram.scale.z)
        endArray()

        name("billboardMode").value(hologram.billboardMode.name)
        name("updateRate").value(hologram.updateRate)
        name("viewRange").value(hologram.viewRange)
        hologram.conditionalPlaceholder?.let { name("conditionalPlaceholder").value(it) }
        endObject()
    }

    fun deleteHologram(name: String) = runCatching {
        configDir.resolve("$name.json").toFile().let {
            if (it.exists()) it.delete()
        }
        holograms.remove(name)
    }.onFailure {
        HoloDisplays.LOGGER.error("Failed to delete hologram $name", it)
    }
}