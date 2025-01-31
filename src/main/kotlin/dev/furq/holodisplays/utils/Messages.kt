package dev.furq.holodisplays.utils

import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object Messages {
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
        INVALID_DISPLAY_TYPE,
        INVALID_ITEM,
        INVALID_BLOCK,
        INVALID_LINE_INDEX,
        INVALID_OFFSET,
        INVALID_TEXT_OPACITY,
        INVALID_UPDATE_RATE,
        INVALID_ROTATION,
        INVALID_CONDITION;

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
            INVALID_ITEM -> "Invalid item ID provided"
            INVALID_BLOCK -> "Invalid block ID provided"
            INVALID_LINE_INDEX -> "Invalid line index"
            INVALID_OFFSET -> "Invalid offset values"
            INVALID_TEXT_OPACITY -> "Text opacity must be between 1 and 100"
            INVALID_UPDATE_RATE -> "Update rate must be between 1 and 100 ticks"
            INVALID_ROTATION -> "Invalid rotation values"
            INVALID_CONDITION -> "Invalid condition format. Use format: %placeholder% operator value"
            else -> "An unknown error occurred"
        }
    }

    enum class SuccessType(private val messageProvider: (String?) -> String) {
        HOLOGRAM_CREATED({ name -> "Successfully created hologram " + Formatting.WHITE + "'$name'" }),
        HOLOGRAM_DELETED({ name -> "Successfully deleted hologram " + Formatting.WHITE + "'$name'" }),
        HOLOGRAM_UPDATED({ details -> "Successfully updated hologram " + Formatting.WHITE + details }),
        DISPLAY_CREATED({ details -> "Successfully created " + Formatting.WHITE + details }),
        DISPLAY_DELETED({ name -> "Successfully deleted display " + Formatting.WHITE + "'$name'" }),
        DISPLAY_UPDATED({ details -> "Successfully updated display " + Formatting.WHITE + details }),
        LINE_ADDED({ details -> "Successfully added display " + Formatting.WHITE + "'$details'" + Formatting.GREEN + " to hologram" }),
        LINE_REMOVED({ details -> "Successfully removed line " + Formatting.WHITE + details + Formatting.GREEN + " from hologram" }),
        POSITION_UPDATED({ details -> "Successfully updated position to " + Formatting.WHITE + details }),
        SCALE_UPDATED({ details -> "Successfully updated scale to " + Formatting.WHITE + details }),
        BILLBOARD_UPDATED({ details -> "Successfully updated billboard mode to " + Formatting.WHITE + details }),
        ROTATION_UPDATED({ details -> "Successfully updated rotation to " + Formatting.WHITE + details }),
        TEXT_UPDATED({ details -> "Successfully updated text to " + Formatting.WHITE + "'$details'" }),
        BACKGROUND_UPDATED({ details -> "Successfully updated background to " + Formatting.WHITE + details }),
        OPACITY_UPDATED({ details -> "Successfully updated opacity to " + Formatting.WHITE + details + "%" });

        fun getMessage(details: String? = null): String = messageProvider(details)
    }

    fun sendError(source: ServerCommandSource, type: ErrorType) {
        source.sendError(Text.literal(type.getMessage()).formatted(Formatting.RED))
    }

    fun sendFeedback(source: ServerCommandSource, type: SuccessType, details: String? = null) {
        source.sendFeedback({ Text.literal(type.getMessage(details)).formatted(Formatting.GREEN) }, false)
    }
}