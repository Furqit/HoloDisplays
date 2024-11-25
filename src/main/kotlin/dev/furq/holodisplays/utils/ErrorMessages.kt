package dev.furq.holodisplays.utils

import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ErrorMessages {
    enum class ErrorType {
        HOLOGRAM_EXISTS,
        HOLOGRAM_NOT_FOUND,
        DISPLAY_EXISTS,
        DISPLAY_NOT_FOUND,
        INVALID_SCALE,
        INVALID_BILLBOARD,
        INVALID_VIEW_RANGE,
        LINE_NOT_FOUND,
        INVALID_ARGUMENT,
        INVALID_COLOR,
        INVALID_ALIGNMENT,
        INVALID_DISPLAY_TYPE;

        fun getMessage(): String = when (this) {
            HOLOGRAM_EXISTS -> "A hologram with this name already exists"
            HOLOGRAM_NOT_FOUND -> "Could not find a hologram with this name"
            DISPLAY_EXISTS -> "A display with this name already exists"
            DISPLAY_NOT_FOUND -> "Could not find a display with this name"
            INVALID_SCALE -> "Scale must be at least 0.1"
            INVALID_BILLBOARD -> "Invalid billboard mode. Must be 'fixed', 'vertical', 'horizontal', or 'center'"
            INVALID_VIEW_RANGE -> "View range must be between 1 and 128 blocks"
            LINE_NOT_FOUND -> "Could not find a line at this index"
            INVALID_ARGUMENT -> "Invalid argument provided"
            INVALID_COLOR -> "Invalid color format. Use hex format (e.g., FFFFFF)"
            INVALID_ALIGNMENT -> "Invalid alignment. Must be 'left', 'center', or 'right'"
            INVALID_DISPLAY_TYPE -> "Invalid display type. Must be 'none', 'thirdperson_lefthand', 'thirdperson_righthand', 'firstperson_lefthand', 'firstperson_righthand', 'head', 'gui', 'ground', or 'fixed'"
        }
    }

    fun sendError(source: ServerCommandSource, type: ErrorType) {
        source.sendError(Text.literal(type.getMessage()).formatted(Formatting.RED))
    }
}