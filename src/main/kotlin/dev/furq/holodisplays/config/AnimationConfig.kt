package dev.furq.holodisplays.config

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.AnimationData
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.io.FileFilter
import java.nio.file.Path

object AnimationConfig : Config {
    override lateinit var configDir: Path
    private val animations = mutableMapOf<String, AnimationData>()
    private val jsonFilter = FileFilter { it.extension == "json" }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("animations")
        super.init(baseDir)
    }

    override fun reload() {
        animations.clear()
        runCatching {
            configDir.toFile().listFiles(jsonFilter)?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    animations[file.nameWithoutExtension] = parseAnimationData(json)
                }
            }
        }.onFailure { HoloDisplays.LOGGER.error("Failed to load animations", it) }
    }

    private fun parseAnimationData(json: JsonReader): AnimationData = json.run {
        val builder = AnimationData.Builder()
        beginObject()

        while (hasNext()) {
            when (nextName()) {
                "frames" -> {
                    beginArray()
                    while (hasNext()) {
                        builder.addFrame(nextString())
                    }
                    endArray()
                }

                "interval" -> builder.interval = nextInt()
                else -> skipValue()
            }
        }
        endObject()

        builder.build()
    }

    fun getAnimation(name: String) = animations[name]
    fun getAnimations() = animations.toMap()

    private fun saveAnimation(name: String, animation: AnimationData) {
        animations[name] = animation

        runCatching {
            val file = configDir.resolve("$name.json").toFile()
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
            configDir.resolve("$name.json").toFile().let {
                if (it.exists()) it.delete()
            }
            animations.remove(name)
        }.onFailure { HoloDisplays.LOGGER.error("Failed to delete animation $name", it) }
    }
}