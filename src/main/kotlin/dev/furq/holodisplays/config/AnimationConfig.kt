package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.AnimationData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object AnimationConfig : Config {
    override lateinit var configDir: Path
    private val animations = ConcurrentHashMap<String, AnimationData>()
    private val json = sharedJson

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("animations")
        super.init(baseDir)
    }

    override fun reload() {
        val files = configDir.toFile().listFiles { it.extension == "json" }
            ?: throw ConfigException("Failed to list animation config files")
        val loaded = mutableMapOf<String, AnimationData>()
        files.forEach { file ->
            safeCall {
                val jsonContent = file.readText()
                val animationData = json.decodeFromString<AnimationData>(jsonContent)
                loaded[file.nameWithoutExtension] = animationData
            }
        }
        animations.clear()
        animations.putAll(loaded)
    }


    fun getAnimation(name: String) = animations[name]

    fun saveAnimation(name: String, animation: AnimationData) = safeCall {
        animations[name] = animation
        val file = configDir.resolve("$name.json").toFile()
        file.parentFile.mkdirs()

        val jsonContent = json.encodeToString(animation)
        file.writeText(jsonContent)
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