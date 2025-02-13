package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.AnimationData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler
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

    override fun reload() = ErrorHandler.withCatch {
        animations.clear()
        val files =
            configDir.toFile().listFiles(jsonFilter) ?: throw ConfigException("Failed to list animation config files")

        files.forEach { file ->
            JsonReader.json5(file.inputStream().reader()).use { json ->
                animations[file.nameWithoutExtension] = parseAnimationData(json)
            }
        }
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

    fun saveAnimation(name: String, animation: AnimationData) = ErrorHandler.withCatch {
        animations[name] = animation
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream().writer().use { writer ->
            JsonWriter.json(writer).use { json -> writeAnimation(json, animation) }
        }
    }

    private fun writeAnimation(json: JsonWriter, animation: AnimationData) = json.run {
        beginObject()
        name("frames").beginArray()
        animation.frames.forEach { value(it.text) }
        endArray()
        name("interval").value(animation.interval)
        endObject()
    }

    fun deleteAnimation(name: String) = ErrorHandler.withCatch {
        val file = configDir.resolve("$name.json").toFile()
        if (!file.exists()) {
            throw ConfigException("Animation config file for $name does not exist")
        }
        if (!file.delete()) {
            throw ConfigException("Failed to delete animation config file for $name")
        }
        animations.remove(name)
    }
}