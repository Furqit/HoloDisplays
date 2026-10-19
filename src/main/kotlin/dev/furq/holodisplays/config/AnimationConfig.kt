package dev.furq.holodisplays.config

import dev.furq.holodisplays.data.AnimationData
import dev.furq.holodisplays.data.TransformAnimationData
import dev.furq.holodisplays.handlers.ConfigException
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object AnimationConfig : Config {
    private val bundledAnimations = listOf("drop_item")

    override lateinit var configDir: Path
    private val textAnimations = ConcurrentHashMap<String, AnimationData>()
    private val transformAnimations = ConcurrentHashMap<String, TransformAnimationData>()
    private val json = sharedJson

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("animations")
        writeBundledAnimations()
        super.init(baseDir)
    }

    private fun writeBundledAnimations() = bundledAnimations.forEach { name ->
        val file = configDir.resolve("$name.json").toFile()
        if (!file.exists()) {
            AnimationConfig::class.java.getResourceAsStream("/holodisplays/animations/$name.json")?.use { stream ->
                file.parentFile.mkdirs()
                file.outputStream().use { stream.copyTo(it) }
            }
        }
    }

    override fun reload() {
        val files = configDir.toFile().listFiles { it.extension == "json" }
            ?: throw ConfigException("Failed to list animation config files")
        val loadedText = mutableMapOf<String, AnimationData>()
        val loadedTransform = mutableMapOf<String, TransformAnimationData>()
        files.forEach { file ->
            safeCall {
                val jsonContent = file.readText()
                val text = runCatching { json.decodeFromString<AnimationData>(jsonContent) }.getOrNull()
                if (text != null) {
                    loadedText[file.nameWithoutExtension] = text
                } else {
                    val transform = runCatching { json.decodeFromString<TransformAnimationData>(jsonContent) }.getOrNull()
                        ?: throw ConfigException("Invalid animation config file: ${file.name}")
                    loadedTransform[file.nameWithoutExtension] = transform
                }
            }
        }
        textAnimations.clear()
        textAnimations.putAll(loadedText)
        transformAnimations.clear()
        transformAnimations.putAll(loadedTransform)
    }


    fun getAnimation(name: String) = textAnimations[name]

    fun getTextAnimationNames(): Set<String> = textAnimations.keys

    fun setTextAnimation(name: String, animation: AnimationData) {
        textAnimations[name] = animation
    }

    fun removeTextAnimation(name: String): AnimationData? = textAnimations.remove(name)

    fun getTransformAnimation(name: String) = transformAnimations[name]

    fun getTransformAnimationNames(): Set<String> = transformAnimations.keys

    fun setTransformAnimation(name: String, animation: TransformAnimationData) {
        transformAnimations[name] = animation
    }

    fun removeTransformAnimation(name: String): TransformAnimationData? = transformAnimations.remove(name)
}