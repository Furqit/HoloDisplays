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
        HOLOGRAM_CREATED({ name -> "Successfully created hologram${name?.let { " '$it'" } ?: ""}" }),
        HOLOGRAM_DELETED({ name -> "Successfully deleted hologram${name?.let { " '$it'" } ?: ""}" }),
        HOLOGRAM_UPDATED({ details -> "Successfully updated hologram${details?.let { ": $it" } ?: ""}" }),
        DISPLAY_CREATED({ details -> "Successfully created display${details?.let { " $it" } ?: ""}" }),
        DISPLAY_DELETED({ name -> "Successfully deleted display${name?.let { " '$it'" } ?: ""}" }),
        DISPLAY_UPDATED({ details -> "Successfully updated display${details?.let { ": $it" } ?: ""}" }),
        LINE_ADDED({ details -> "Successfully added display${details?.let { " '$it'" } ?: ""} to hologram" }),
        LINE_REMOVED({ details -> "Successfully removed line${details?.let { " $it" } ?: ""} from hologram" }),
        POSITION_UPDATED({ details -> "Successfully updated position${details?.let { " to $it" } ?: ""}" }),
        SCALE_UPDATED({ details -> "Successfully updated scale${details?.let { " to $it" } ?: ""}" }),
        BILLBOARD_UPDATED({ details -> "Successfully updated billboard mode${details?.let { " to $it" } ?: ""}" }),
        ROTATION_UPDATED({ details -> "Successfully updated rotation${details?.let { " to $it" } ?: ""}" }),
        TEXT_UPDATED({ details -> "Successfully updated text${details?.let { " to '$it'" } ?: ""}" }),
        BACKGROUND_UPDATED({ details -> "Successfully updated background${details?.let { " to $it" } ?: ""}" }),
        OPACITY_UPDATED({ details -> "Successfully updated opacity${details?.let { " to $it%" } ?: ""}" });

        fun getMessage(details: String? = null): String = messageProvider(details)
    }

    fun sendError(source: ServerCommandSource, type: ErrorType) {
        source.sendError(Text.literal(type.getMessage()).formatted(Formatting.RED))
    }

    fun sendFeedback(source: ServerCommandSource, type: SuccessType, details: String? = null) {
        source.sendFeedback({ Text.literal(type.getMessage(details)).formatted(Formatting.GREEN) }, false)
    }
}