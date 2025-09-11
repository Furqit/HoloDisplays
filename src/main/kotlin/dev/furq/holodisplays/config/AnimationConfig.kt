package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.AnimationData
import dev.furq.holodisplays.handlers.ConfigException
import org.quiltmc.parsers.json.JsonReader
import org.quiltmc.parsers.json.JsonWriter
import java.nio.file.Path

object AnimationConfig : Config {
    override lateinit var configDir: Path
    private val animations = mutableMapOf<String, AnimationData>()

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("animations")
        super.init(baseDir)
    }

    override fun reload() {
        animations.clear()
        configDir.toFile().listFiles(JsonUtils.jsonFilter)
            ?.forEach { file ->
                JsonReader.json5(file.inputStream().reader()).use { json ->
                    animations[file.nameWithoutExtension] = parseAnimationData(json)
                }
            }
            ?: throw ConfigException("Failed to list animation config files")
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

    fun saveAnimation(name: String, animation: AnimationData) {
        animations[name] = animation
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        file.outputStream().writer().use { writer ->
            JsonWriter.json(writer).use { json -> writeAnimation(json, animation) }
        }
    }

    private fun writeAnimation(json: JsonWriter, animation: AnimationData) = json.run {
        beginObject()
        JsonUtils.writeStringList(this, "frames", animation.frames.map { it.text })
        name("interval").value(animation.interval)
        endObject()
    }

    fun deleteAnimation(name: String) {
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