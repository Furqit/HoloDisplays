package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object ErrorHandler {

    fun handle(exception: Exception, source: ServerCommandSource? = null) {
        val errorType = when (exception) {
            is HoloDisplaysException -> exception::class.simpleName?.removeSuffix("Exception") ?: "Error"
            else -> "Unexpected"
        }

        val logMessage = "[$errorType Error] ${exception.message}"

        if (exception is HoloDisplaysException) {
            HoloDisplays.LOGGER.error(logMessage)
        } else {
            HoloDisplays.LOGGER.error(logMessage, exception)
        }

        source?.sendError(Text.literal(
            when (exception) {
                is HoloDisplaysException -> "§c$errorType: ${exception.message}"
                else -> "§cAn unexpected error occurred. Check console for details"
            }
        ))
    }

    inline fun <T> safeCall(
        source: ServerCommandSource? = null,
        default: T? = null,
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            handle(e, source)
            default
        }
    }
}

sealed class HoloDisplaysException(message: String) : Exception(message)
class DisplayException(message: String) : HoloDisplaysException(message)
class HologramException(message: String) : HoloDisplaysException(message)
class ConfigException(message: String) : HoloDisplaysException(message)