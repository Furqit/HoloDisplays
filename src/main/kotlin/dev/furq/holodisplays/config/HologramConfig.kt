package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.nio.file.Path

object HologramConfig : Config {
    override lateinit var configDir: Path
    private val holograms = mutableMapOf<String, HologramData>()

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("holograms")
        super.init(baseDir)
    }

    override fun reload() {
        holograms.clear()
        configDir.toFile().listFiles(JsonUtils.jsonFilter)
            ?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    holograms[file.nameWithoutExtension] = parseHologramData(json)
                }
            }
            ?: throw ConfigException("Failed to list hologram config files")
    }

    private fun parseHologramData(json: JsonReader): HologramData = json.run {
        val builder = HologramData.Builder()
        beginObject()

        while (hasNext()) {
            when (nextName()) {
                "displays" -> builder.displays = parseDisplayLines()
                "position" -> parsePosition().let { (world, position) ->
                    builder.world = world
                    builder.position = position
                }

                "rotation" -> builder.rotation = JsonUtils.parseVector3f(this)
                "scale" -> builder.scale = JsonUtils.parseVector3f(this)
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
                    "offset" -> offset = JsonUtils.parseVector3f(this)
                    else -> skipValue()
                }
            }
            endObject()

            name.takeIf { it.isNotEmpty() }?.let {
                lines.add(HologramData.DisplayLine(it, offset))
            }
        }
        endArray()
        return lines
    }

    private fun JsonReader.parsePosition(): Pair<String, Vector3f> {
        var world = "minecraft:overworld"
        var position = Vector3f()

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "world" -> world = nextString()
                "x" -> position.x = nextDouble().toFloat()
                "y" -> position.y = nextDouble().toFloat()
                "z" -> position.z = nextDouble().toFloat()
                else -> skipValue()
            }
        }
        endObject()
        return world to position
    }

    fun getHologram(name: String): HologramData? = holograms[name]
    fun getHolograms(): Map<String, HologramData> = holograms
    fun exists(name: String): Boolean = holograms.containsKey(name)

    fun saveHologram(name: String, hologram: HologramData) = safeCall {
        holograms[name] = hologram
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream().writer().use { writer ->
            JsonWriter.json(writer).use { json -> writeHologram(json, hologram) }
        }
    }

    private fun writeHologram(json: JsonWriter, hologram: HologramData) = json.run {
        beginObject()

        name("displays").beginArray()
        hologram.displays.forEach { line ->
            beginObject()
            name("name").value(line.displayId)
            JsonUtils.writeVector3f(this, "offset", line.offset)
            endObject()
        }
        endArray()

        name("position").beginObject()
        name("world").value(hologram.world)
        name("x").value(hologram.position.x)
        name("y").value(hologram.position.y)
        name("z").value(hologram.position.z)
        endObject()

        JsonUtils.writeVector3f(this, "rotation", hologram.rotation)
        JsonUtils.writeVector3f(this, "scale", hologram.scale)

        name("billboardMode").value(hologram.billboardMode.name)
        name("updateRate").value(hologram.updateRate)
        name("viewRange").value(hologram.viewRange)
        hologram.conditionalPlaceholder?.let { name("conditionalPlaceholder").value(it) }
        endObject()
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