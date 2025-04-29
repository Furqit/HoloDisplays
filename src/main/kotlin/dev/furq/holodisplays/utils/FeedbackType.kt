package dev.furq.holodisplays.utils

enum class FeedbackType(private val template: String) {
    // Success Messages
    HOLOGRAM_CREATED("Successfully created hologram '{name}'"),
    HOLOGRAM_DELETED("Successfully removed hologram '{name}'"),
    HOLOGRAM_UPDATED("Hologram {detail}"),
    DISPLAY_CREATED("Successfully created new {type} display '{name}'"),
    DISPLAY_DELETED("Successfully removed display '{name}'"),
    DISPLAY_UPDATED("Display {detail}"),
    DISPLAY_ADDED("Successfully added display '{name}' to hologram"),
    DISPLAY_REMOVED("Successfully removed display '{name}' from hologram"),
    POSITION_UPDATED("Position set to ({x}, {y}, {z})"),
    SCALE_UPDATED("Scale set to ({x}, {y}, {z})"),
    BILLBOARD_UPDATED("Billboard mode set to '{mode}'"),
    ROTATION_UPDATED("Rotation set to (pitch: {pitch}, yaw: {yaw}, roll: {roll})"),
    TEXT_UPDATED("Text content updated to '{text}'"),
    BACKGROUND_UPDATED("Background color set to #{color} with {opacity}% opacity"),
    OPACITY_UPDATED("Text opacity set to {opacity}%"),
    RELOAD_SUCCESS("Successfully reloaded all configurations!"),
    LINE_WIDTH_UPDATED("Line width updated to {width}"),
    SEE_THROUGH_UPDATED("See through {enabled}"),
    ITEM_ID_UPDATED("Item ID updated to {id}"),
    BLOCK_ID_UPDATED("Block ID updated to {id}"),
    OFFSET_UPDATED("Offset updated to x: {x}, y: {y}, z: {z}"),

    // Error Messages
    HOLOGRAM_EXISTS("A hologram named '{name}' already exists"),
    HOLOGRAM_NOT_FOUND("No hologram found with name '{name}'"),
    DISPLAY_EXISTS("A display named '{name}' already exists"),
    DISPLAY_NOT_FOUND("No display found with name '{name}'"),
    INVALID_SCALE("Invalid scale value - must be at least 0.1"),
    INVALID_BILLBOARD("Invalid billboard mode - must be one of: fixed, vertical, horizontal, center"),
    INVALID_VIEW_RANGE("Invalid view range - must be between 1 and 128 blocks"),
    INVALID_COLOR("Invalid color format - must be a hex color code (e.g., FFFFFF)"),
    INVALID_ALIGNMENT("Invalid alignment - must be one of: left, center, right"),
    INVALID_DISPLAY_TYPE("Invalid display type - must be one of: none, thirdperson_lefthand, thirdperson_righthand, firstperson_lefthand, firstperson_righthand, head, gui, ground, fixed"),
    INVALID_ITEM("Invalid item ID - item does not exist"),
    INVALID_BLOCK("Invalid block ID - block does not exist"),
    INVALID_TEXT_OPACITY("Invalid text opacity - must be between 1 and 100"),
    INVALID_UPDATE_RATE("Invalid update rate - must be between 1 and 100 ticks"),
    INVALID_ROTATION("Invalid rotation values - must be valid angles"),
    INVALID_CONDITION("Invalid condition format - must follow pattern: %placeholder% operator value"),
    DISPLAY_ALREADY_ADDED("Display '{name}' is already added to this hologram"),
    PLAYER_ONLY("This command can only be executed by players");

    fun format(vararg params: Pair<String, Any>): String {
        var message = template
        params.forEach { (key, value) ->
            message = message.replace("{$key}", value.toString())
        }
        return message
    }

    val isError: Boolean
        get() = ordinal >= HOLOGRAM_EXISTS.ordinal
} 