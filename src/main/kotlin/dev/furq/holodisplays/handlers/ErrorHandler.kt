package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ErrorHandler {
    fun handle(exception: Exception, source: ServerCommandSource? = null) {
        when (exception) {
            is HoloDisplaysException -> HoloDisplays.LOGGER.error("[${exception::class.simpleName?.removeSuffix("Exception")} Error] ${exception.message}")
            else -> HoloDisplays.LOGGER.error("[Unexpected Error] An unexpected error occurred", exception)
        }

        source?.player?.let {
            val errorMessage = when (exception) {
                is HoloDisplaysException -> "§c${exception::class.simpleName?.removeSuffix("Exception") ?: "Error"}: ${exception.message}"
                else -> "§cAn unexpected error occurred. Check console for details"
            }
            source.sendError(Text.literal(errorMessage))
        }
    }

    inline fun withCatch(source: ServerCommandSource? = null, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            handle(e, source)
        }
    }

    inline fun <T> withCatch(block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        handle(e)
        null
    }

    inline fun <T> withCatch(source: ServerCommandSource? = null, block: () -> T): T? = try {
        block()
    } catch (e: Exception) {
        handle(e, source)
        null
    }

}

sealed class HoloDisplaysException(message: String) : Exception(message)
class DisplayException(message: String) : HoloDisplaysException(message)
class HologramException(message: String) : HoloDisplaysException(message)
class ConfigException(message: String) : HoloDisplaysException(message)