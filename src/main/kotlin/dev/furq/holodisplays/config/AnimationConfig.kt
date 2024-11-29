package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.AnimationData
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object AnimationConfig : Config {
    private lateinit var animationsDir: Path
    private val animations = mutableMapOf<String, AnimationData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(configDir: Path) {
        animationsDir = configDir.resolve("animations").also {
            if (!it.exists()) it.createDirectories()
        }
        loadAnimations()
    }

    private fun loadAnimations() {
        animations.clear()
        runCatching {
            animationsDir.toFile().listFiles(jsonFilter)?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    animations[file.nameWithoutExtension] = parseAnimationData(json)
                }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to load animations", it) }
    }

    private fun parseAnimationData(json: JsonReader) = json.run {
        beginObject()
        var frames = emptyList<AnimationData.Frame>()
        var interval = 20

        while (hasNext()) {
            when (nextName()) {
                "frames" -> frames = parseFrames()
                "interval" -> interval = nextInt()
                else -> skipValue()
            }
        }
        endObject()
        AnimationData(frames, interval)
    }

    private fun JsonReader.parseFrames(): List<AnimationData.Frame> {
        val frames = mutableListOf<AnimationData.Frame>()
        beginArray()
        while (hasNext()) {
            frames += AnimationData.Frame(nextString())
        }
        endArray()
        return frames
    }

    fun getAnimation(name: String) = animations[name]
    fun getAnimations() = animations.toMap()

    override fun reload() {
        loadAnimations()
    }

    fun saveAnimation(name: String, animation: AnimationData) {
        animations[name] = animation

        runCatching {
            val file = animationsDir.resolve("$name.json").toFile()
            file.parentFile.mkdirs()

            file.outputStream().writer().use { writer ->
                JsonWriter.json(writer).use { json -> writeAnimation(json, animation) }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to save animation $name", it) }
    }

    private fun writeAnimation(json: JsonWriter, animation: AnimationData) = json.run {
        beginObject()
        name("frames").beginArray()
        animation.frames.forEach { value(it.text) }
        endArray()
        name("interval").value(animation.interval)
        endObject()
    }

    fun deleteAnimation(name: String) {
        runCatching {
            animationsDir.resolve("$name.json").toFile().let {
                if (it.exists()) it.delete()
            }
            animations.remove(name)
        }.onFailure { HoloDisplays.LOGGER.error("Failed to delete animation $name", it) }
    }
}