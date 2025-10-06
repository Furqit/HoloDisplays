package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.AnimationData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.nio.file.Path

object AnimationConfig : Config {
    override lateinit var configDir: Path
    private val animations = mutableMapOf<String, AnimationData>()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        allowTrailingComma = true
        allowComments = true
    }

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("animations")
        super.init(baseDir)
    }

    override fun reload() {
        animations.clear()
        configDir.toFile().listFiles { it.extension == "json" }
            ?.forEach { file ->
                val jsonContent = file.readText()
                val animationData = json.decodeFromString<AnimationData>(jsonContent)
                animations[file.nameWithoutExtension] = animationData
            }
            ?: throw ConfigException("Failed to list animation config files")
    }


    fun getAnimation(name: String) = animations[name]
    fun getAnimations() = animations.toMap()

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