package dev.furq.holodisplays.config

import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.mixin.ServerPlayerAccessor
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Lang : Config {
    private const val DEFAULT_LOCALE = "en_us"
    private val bundledLocales = listOf(DEFAULT_LOCALE, "zh_cn")
    private val registry = ConcurrentHashMap<String, MutableMap<String, String>>()
    private val json = sharedJson

    override lateinit var configDir: Path

    override fun init(baseDir: Path) {
        configDir = baseDir.resolve("lang")
        writeBundledLangs()
        super.init(baseDir)
    }

    private fun writeBundledLangs() = bundledLocales.forEach { locale ->
        val file = configDir.resolve("$locale.json").toFile()
        if (!file.exists()) {
            Lang::class.java.getResourceAsStream("/holodisplays/lang/$locale.json")?.use { stream ->
                file.parentFile.mkdirs()
                file.outputStream().use { stream.copyTo(it) }
            }
        }
    }

    override fun reload() {
        val loaded = mutableMapOf<String, MutableMap<String, String>>()
        bundledLocales.forEach { locale ->
            readBundled(locale)?.let { entries -> loaded.getOrPut(locale) { mutableMapOf() }.putAll(entries) }
        }
        configDir.toFile().listFiles { it.extension == "json" }?.forEach { file ->
            safeCall {
                val entries: Map<String, String> = json.decodeFromString(file.readText())
                loaded.getOrPut(file.nameWithoutExtension.lowercase(Locale.US)) { mutableMapOf() }.putAll(entries)
            }
        }
        registry.clear()
        registry.putAll(loaded)
    }

    private fun readBundled(locale: String): Map<String, String>? = runCatching {
        Lang::class.java.getResourceAsStream("/holodisplays/lang/$locale.json")?.use { stream ->
            json.decodeFromString<Map<String, String>>(stream.readAllBytes().decodeToString())
        }
    }.getOrNull()

    fun localeFor(player: ServerPlayer?): String? {
        player ?: return null
        val raw = (player as ServerPlayerAccessor).getLanguage() ?: return null
        return raw.trim().lowercase(Locale.US).ifBlank { null }
    }

    fun template(key: String, locale: String?): String? {
        if (locale == null) return null
        val normalized = locale.lowercase(Locale.US)
        val localized = registry[normalized]?.get(key)
        if (localized != null) return localized
        if (normalized == DEFAULT_LOCALE) return null
        return registry[DEFAULT_LOCALE]?.get(key)
    }

    fun get(player: ServerPlayer?, key: String, default: String, vararg args: Pair<String, Any>): String =
        substitute(template(key, localeFor(player)) ?: default, args)

    fun enumHint(player: ServerPlayer?, group: String, token: String): String =
        template("gui.enum.$group.${token.lowercase(Locale.US)}", localeFor(player)) ?: token

    internal fun substitute(text: String, args: Array<out Pair<String, Any>>): String =
        args.fold(text) { message, (argKey, value) ->
            message.replace("{$argKey}", value.toString())
        }
}
