package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ErrorHandler {
    private fun handle(exception: Exception, source: ServerCommandSource? = null) {
        when (exception) {
            is HoloDisplaysException -> handleHoloDisplaysException(exception)
            else -> handleGenericException(exception)
        }

        if (source?.player != null) {
            source.sendError(Text.literal("An error occurred. Check console for details.").formatted(Formatting.RED))
        }
    }

    private fun handleHoloDisplaysException(exception: HoloDisplaysException) {
        HoloDisplays.LOGGER.error("[${exception.javaClass.simpleName.removeSuffix("Exception")} Error] ${exception.message}")
    }

    private fun handleGenericException(exception: Exception) {
        HoloDisplays.LOGGER.error("[Unexpected Error] An unexpected error occurred", exception)
    }

    fun withCatch(source: ServerCommandSource? = null, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            handle(e, source)
        }
    }

    fun <T> withCatch(block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            handle(e)
            null
        }
    }

    fun <T> withCatch(source: ServerCommandSource? = null, block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            handle(e, source)
            null
        }
    }

    suspend fun withCatchSuspend(source: ServerCommandSource? = null, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            handle(e, source)
        }
    }
}

sealed class HoloDisplaysException(message: String) : Exception(message)
class DisplayException(message: String) : HoloDisplaysException(message)
class HologramException(message: String) : HoloDisplaysException(message)
class ConfigException(message: String) : HoloDisplaysException(message)