package dev.furq.holodisplays.utils

enum class FeedbackType(private val template: String, val isError: Boolean = false) {
    // Success Messages
    HOLOGRAM_CREATED("Hologram '{name}' created successfully"),
    HOLOGRAM_DELETED("Hologram '{name}' has been removed"),
    HOLOGRAM_UPDATED("Hologram updated: {detail}"),
    DISPLAY_CREATED("Created new {type} display '{name}'"),
    DISPLAY_DELETED("Display '{name}' has been removed"),
    DISPLAY_UPDATED("Display updated: {detail}"),
    DISPLAY_ADDED("Display '{name}' added to hologram"),
    DISPLAY_REMOVED("Display '{name}' removed from hologram"),
    POSITION_UPDATED("Position updated to ({x}, {y}, {z})"),
    SCALE_UPDATED("Scale updated to ({x}, {y}, {z})"),
    BILLBOARD_UPDATED("Billboard mode changed to '{mode}'"),
    ROTATION_UPDATED("Rotation updated to (pitch: {pitch}°, yaw: {yaw}°, roll: {roll}°)"),
    TEXT_UPDATED("Text updated to: '{text}'"),
    BACKGROUND_UPDATED("Background set to #{color} with {opacity}% opacity"),
    OPACITY_UPDATED("Text opacity set to {opacity}%"),
    RELOAD_SUCCESS("All configurations reloaded successfully"),
    LINE_WIDTH_UPDATED("Line width set to {width}"),
    SEE_THROUGH_UPDATED("See-through mode {enabled}"),
    ITEM_ID_UPDATED("Item ID changed to {id}"),
    BLOCK_ID_UPDATED("Block ID changed to {id}"),
    ENTITY_ID_UPDATED("Entity ID changed to {id}"),
    OFFSET_UPDATED("Offset updated to x: {x}, y: {y}, z: {z}"),

    // Error Messages
    HOLOGRAM_EXISTS("Hologram '{name}' already exists. Choose a different name", true),
    HOLOGRAM_NOT_FOUND("Hologram '{name}' not found. Check the name and try again", true),
    DISPLAY_EXISTS("Display '{name}' already exists. Choose a different name", true),
    DISPLAY_NOT_FOUND("Display '{name}' not found. Check the name and try again", true),
    INVALID_SCALE("Invalid scale value. Must be at least 0.1", true),
    INVALID_BILLBOARD("Invalid billboard mode. Use: fixed, vertical, horizontal, center", true),
    INVALID_VIEW_RANGE("Invalid view range. Must be between 1 and 128 blocks", true),
    INVALID_COLOR("Invalid color format. Use hex color code (e.g., FFFFFF)", true),
    INVALID_ALIGNMENT("Invalid alignment. Use: left, center, right", true),
    INVALID_DISPLAY_TYPE("Invalid display type. Use: none, thirdperson_lefthand, thirdperson_righthand, firstperson_lefthand, firstperson_righthand, head, gui, ground, fixed", true),
    INVALID_ITEM("Invalid item ID. Item does not exist in the game", true),
    INVALID_BLOCK("Invalid block ID. Block does not exist in the game", true),
    INVALID_ENTITY("Invalid entity ID. Entity does not exist in the game", true),
    INVALID_TEXT_OPACITY("Invalid text opacity. Must be between 1 and 100", true),
    INVALID_UPDATE_RATE("Invalid update rate. Must be at least 1 tick", true),
    INVALID_ROTATION("Invalid rotation values. Must be valid angles", true),
    INVALID_CONDITION("Invalid condition format. Use: %placeholder% operator value", true),
    DISPLAY_ALREADY_ADDED("Display '{name}' is already added to this hologram", true),
    PLAYER_ONLY("This command can only be used by players", true);

    fun format(vararg params: Pair<String, Any>): String =
        params.fold(template) { message, (key, value) ->
            message.replace("{$key}", value.toString())
        }
}