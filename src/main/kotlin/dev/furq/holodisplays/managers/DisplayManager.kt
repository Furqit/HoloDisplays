package dev.furq.holodisplays.managers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.handlers.DisplayHandler.DisplayProperty.*
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import org.joml.Vector3f

class DisplayManager {
    fun createTextDisplay(name: String, text: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_EXISTS, "name" to name)
            return false
        }

        val display = DisplayData(
            display = TextDisplay(
                lines = mutableListOf(text)
            )
        )

        DisplayConfig.saveDisplay(name, display)
        FeedbackManager.send(source, FeedbackType.DISPLAY_CREATED, "type" to "text", "name" to name)
        return true
    }

    fun createItemDisplay(name: String, itemId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_EXISTS, "name" to name)
            return false
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            FeedbackManager.send(source, FeedbackType.INVALID_ITEM)
            return false
        }

        val display = DisplayData(
            display = ItemDisplay(
                id = fullItemId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        FeedbackManager.send(source, FeedbackType.DISPLAY_CREATED, "type" to "item", "name" to name)
        return true
    }

    fun createBlockDisplay(name: String, blockId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_EXISTS, "name" to name)
            return false
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            FeedbackManager.send(source, FeedbackType.INVALID_BLOCK)
            return false
        }

        val display = DisplayData(
            display = BlockDisplay(
                id = fullBlockId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        FeedbackManager.send(source, FeedbackType.DISPLAY_CREATED, "type" to "block", "name" to name)
        return true
    }

    fun deleteDisplay(name: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        val affectedHolograms = HologramConfig.getHolograms()
            .filter { (_, hologram) ->
                hologram.displays.any { it.displayId == name }
            }

        affectedHolograms.forEach { (hologramName, hologram) ->
            hologram.displays.removeAll { it.displayId == name }
            HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.RemoveLine(0))
        }

        DisplayConfig.deleteDisplay(name)
        FeedbackManager.send(source, FeedbackType.DISPLAY_DELETED, "name" to name)
    }

    fun updateScale(name: String, scale: Vector3f, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        if (scale.x < 0.1f || scale.y < 0.1f || scale.z < 0.1f) {
            FeedbackManager.send(source, FeedbackType.INVALID_SCALE)
            return
        }

        DisplayHandler.updateDisplayProperty(name, Scale(scale))
        FeedbackManager.send(source, FeedbackType.SCALE_UPDATED, *FeedbackManager.formatVector3f(scale))
    }

    fun resetScale(name: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, Scale(Vector3f(1f)))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "scale reset to default")
    }

    fun updateBillboard(name: String, mode: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        val newMode = try {
            BillboardMode.valueOf(mode.uppercase())
        } catch (e: IllegalArgumentException) {
            FeedbackManager.send(source, FeedbackType.INVALID_BILLBOARD)
            return
        }

        DisplayHandler.updateDisplayProperty(name, BillboardMode(newMode))
        FeedbackManager.send(source, FeedbackType.BILLBOARD_UPDATED, "mode" to mode.lowercase())
    }

    fun resetBillboard(name: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, BillboardMode(BillboardMode.CENTER))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "billboard mode reset to center")
    }

    fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, Rotation(Vector3f(pitch, yaw, roll)))
        FeedbackManager.send(source, FeedbackType.ROTATION_UPDATED, *FeedbackManager.formatRotation(pitch, yaw, roll))
    }

    fun resetRotation(name: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, Rotation(Vector3f()))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "rotation reset to default")
    }

    fun updateBackground(name: String, color: String, opacity: Int, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        if (!color.matches(Regex("^[0-9A-Fa-f]{6}$"))) {
            FeedbackManager.send(source, FeedbackType.INVALID_COLOR)
            return
        }

        val opacityHex = ((opacity.coerceIn(1, 100) / 100.0 * 255).toInt())
            .toString(16)
            .padStart(2, '0')
            .uppercase()

        DisplayHandler.updateDisplayProperty(
            name,
            TextBackgroundColor("$opacityHex$color")
        )
        FeedbackManager.send(source, FeedbackType.BACKGROUND_UPDATED, "color" to color, "opacity" to opacity)
    }

    fun resetBackground(name: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, TextBackgroundColor(null))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "background reset to default")
    }

    fun updateTextOpacity(name: String, opacity: Int, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        if (opacity !in 1..100) {
            FeedbackManager.send(source, FeedbackType.INVALID_TEXT_OPACITY)
            return
        }

        DisplayHandler.updateDisplayProperty(name, TextOpacity(opacity))
        FeedbackManager.send(source, FeedbackType.OPACITY_UPDATED, "opacity" to opacity)
    }

    fun updateShadow(name: String, shadow: Boolean, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, TextShadow(shadow))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "shadow ${if (shadow) "enabled" else "disabled"}")
    }

    fun updateAlignment(name: String, alignment: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        val textAlignment = try {
            TextDisplay.TextAlignment.valueOf(alignment.uppercase())
        } catch (e: IllegalArgumentException) {
            FeedbackManager.send(source, FeedbackType.INVALID_ALIGNMENT)
            return
        }

        DisplayHandler.updateDisplayProperty(name, TextAlignment(textAlignment))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "text alignment set to ${alignment.lowercase()}")
    }

    fun updateItemDisplayType(name: String, type: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, ItemDisplayType(type))
        FeedbackManager.send(source, FeedbackType.DISPLAY_UPDATED, "detail" to "item display type set to $type")
    }

    fun updateCustomModelData(name: String, customModelData: Int?, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(name, ItemCustomModelData(customModelData))
        FeedbackManager.send(
            source,
            FeedbackType.DISPLAY_UPDATED,
            "detail" to "custom model data set to ${customModelData ?: "none"}"
        )
    }

    fun updateCondition(name: String, condition: String?, source: ServerCommandSource) {
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return
        }

        DisplayHandler.updateDisplayProperty(
            name,
            ConditionalPlaceholder(condition)
        )
        FeedbackManager.send(
            source,
            FeedbackType.DISPLAY_UPDATED,
            "detail" to "condition set to ${condition ?: "none"}"
        )
    }

    fun updateLineWidth(displayName: String, width: Int, source: ServerCommandSource) {
        if (!DisplayConfig.exists(displayName)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }

        val display = DisplayConfig.getDisplay(displayName)
        if (display?.display !is TextDisplay) {
            FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "text")
            return
        }

        DisplayHandler.updateDisplayProperty(displayName, TextLineWidth(width))
        FeedbackManager.send(source, FeedbackType.LINE_WIDTH_UPDATED, "width" to width.toString())
    }

    fun updateSeeThrough(displayName: String, seeThrough: Boolean, source: ServerCommandSource) {
        if (!DisplayConfig.exists(displayName)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }

        val display = DisplayConfig.getDisplay(displayName)
        if (display?.display !is TextDisplay) {
            FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "text")
            return
        }

        DisplayHandler.updateDisplayProperty(displayName, TextSeeThrough(seeThrough))
        FeedbackManager.send(source, FeedbackType.SEE_THROUGH_UPDATED, "enabled" to seeThrough.toString())
    }

    fun updateItemId(displayName: String, itemId: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(displayName)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }

        val display = DisplayConfig.getDisplay(displayName)
        if (display?.display !is ItemDisplay) {
            FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "item")
            return
        }

        DisplayHandler.updateDisplayProperty(displayName, ItemId(itemId))
        FeedbackManager.send(source, FeedbackType.ITEM_ID_UPDATED, "id" to itemId)
    }

    fun updateBlockId(displayName: String, blockId: String, source: ServerCommandSource) {
        if (!DisplayConfig.exists(displayName)) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }

        val display = DisplayConfig.getDisplay(displayName)
        if (display?.display !is BlockDisplay) {
            FeedbackManager.send(source, FeedbackType.INVALID_DISPLAY_TYPE, "type" to "block")
            return
        }

        DisplayHandler.updateDisplayProperty(displayName, BlockId(blockId))
        FeedbackManager.send(source, FeedbackType.BLOCK_ID_UPDATED, "id" to blockId)
    }
}