package dev.furq.holodisplays.utils

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.Messages.ErrorType
import dev.furq.holodisplays.utils.Messages.SuccessType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.joml.Vector3f

object Utils {
    // Hologram Creation and Deletion
    fun createHologram(name: String, player: ServerPlayerEntity) {
        if (HologramConfig.exists(name)) {
            Messages.sendError(player.commandSource, ErrorType.HOLOGRAM_EXISTS)
            playErrorSound(player.commandSource)
            return
        }

        val defaultDisplayName = "${name}_text"
        val defaultDisplay = DisplayData(
            display = TextDisplay(
                lines = mutableListOf("<gr #ffffff #008000>Hello, %player:name%</gr>")
            )
        )

        DisplayConfig.saveDisplay(defaultDisplayName, defaultDisplay)

        val pos = player.pos
        val hologram = HologramData(
            displays = mutableListOf(HologramData.DisplayLine(defaultDisplayName)),
            position = Vector3f(
                String.format("%.3f", pos.x).toFloat(),
                String.format("%.3f", pos.y).toFloat(),
                String.format("%.3f", pos.z).toFloat()
            ),
            world = player.world.registryKey.value.toString(),
            rotation = Vector3f(),
            scale = Vector3f(1f),
            billboardMode = BillboardMode.CENTER,
            updateRate = 20,
            viewRange = 16.0,
        )

        HologramHandler.createHologram(name, hologram)
        Messages.sendFeedback(player.commandSource, SuccessType.HOLOGRAM_CREATED, name)
        playSuccessSound(player.commandSource)
    }

    fun deleteHologram(name: String, source: ServerCommandSource): Boolean {
        HologramHandler.deleteHologram(name)
        Messages.sendFeedback(source, SuccessType.HOLOGRAM_DELETED, name)
        playSuccessSound(source)
        return true
    }

    // Hologram Properties Management
    fun updateHologramPosition(
        name: String,
        pos: Vector3f,
        worldId: String,
        source: ServerCommandSource
    ): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val formattedPosition = Vector3f(
            String.format("%.3f", pos.x).toFloat(),
            String.format("%.3f", pos.y).toFloat(),
            String.format("%.3f", pos.z).toFloat()
        )

        HologramHandler.updateHologramProperty(
            name,
            HologramHandler.HologramProperty.Position(formattedPosition, worldId)
        )
        Messages.sendFeedback(
            source, SuccessType.POSITION_UPDATED,
            "${formattedPosition.x}, ${formattedPosition.y}, ${formattedPosition.z}"
        )
        playSuccessSound(source)
        return true
    }

    fun updateHologramScale(name: String, scale: Vector3f, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.Scale(scale))
        Messages.sendFeedback(
            source, SuccessType.SCALE_UPDATED,
            "${scale.x}, ${scale.y}, ${scale.z}"
        )
        playSuccessSound(source)
        return true
    }

    fun updateHologramBillboard(name: String, billboard: String, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val newMode = try {
            BillboardMode.valueOf(billboard.uppercase())
        } catch (e: IllegalArgumentException) {
            Messages.sendError(source, ErrorType.INVALID_BILLBOARD)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.BillboardMode(newMode))
        Messages.sendFeedback(source, SuccessType.BILLBOARD_UPDATED, newMode.name.lowercase())
        playSuccessSound(source)
        return true
    }

    fun updateHologramUpdateRate(name: String, rate: Int, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.UpdateRate(rate))
        Messages.sendFeedback(source, SuccessType.HOLOGRAM_UPDATED, "update rate to ${rate}t")
        playSuccessSound(source)
        return true
    }

    fun updateHologramViewRange(name: String, range: Float, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        if (range !in 1f..128f) {
            Messages.sendError(source, ErrorType.INVALID_VIEW_RANGE)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.ViewRange(range.toDouble()))
        Messages.sendFeedback(source, SuccessType.HOLOGRAM_UPDATED, "view range to ${range} blocks")
        playSuccessSound(source)
        return true
    }

    fun updateHologramRotation(
        name: String,
        pitch: Float,
        yaw: Float,
        roll: Float,
        source: ServerCommandSource
    ): Boolean {
        if (!HologramConfig.exists(name)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(
            name,
            HologramHandler.HologramProperty.Rotation(Vector3f(pitch, yaw, roll))
        )
        Messages.sendFeedback(source, SuccessType.ROTATION_UPDATED, "pitch: $pitch, yaw: $yaw, roll: $roll")
        playSuccessSound(source)
        return true
    }

    // Display Creation and Management
    fun createTextDisplay(name: String, text: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = TextDisplay(
                lines = mutableListOf(text)
            )
        )

        DisplayConfig.saveDisplay(name, display)
        Messages.sendFeedback(source, SuccessType.DISPLAY_CREATED, "text display '$name'")
        playSuccessSound(source)
        return true
    }

    fun createItemDisplay(name: String, itemId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            Messages.sendError(source, ErrorType.INVALID_ITEM)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = ItemDisplay(
                id = fullItemId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_CREATED,
            "item display '$name'"
        )
        playSuccessSound(source)
        return true
    }

    fun createBlockDisplay(name: String, blockId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            Messages.sendError(source, ErrorType.INVALID_BLOCK)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = BlockDisplay(
                id = fullBlockId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_CREATED,
            " block display '$name'"
        )
        playSuccessSound(source)
        return true
    }

    // Display Properties Management
    fun updateDisplayText(name: String, text: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Lines(listOf(text)))
        Messages.sendFeedback(source, SuccessType.TEXT_UPDATED, text)
        playSuccessSound(source)
        return true
    }

    fun updateDisplayLineWidth(name: String, width: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.LineWidth(width))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "line width to $width")
        playSuccessSound(source)
        return true
    }

    fun updateDisplaySeeThrough(name: String, seeThrough: Boolean, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.SeeThrough(seeThrough))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "see through to ${if (seeThrough) "enabled" else "disabled"}"
        )
        playSuccessSound(source)
        return true
    }

    fun updateDisplayItem(name: String, itemId: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            Messages.sendError(source, ErrorType.INVALID_ITEM)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.ItemId(fullItemId))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "item to $fullItemId")
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBlock(name: String, blockId: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            Messages.sendError(source, ErrorType.INVALID_BLOCK)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BlockId(fullBlockId))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "block to $fullBlockId")
        playSuccessSound(source)
        return true
    }

    fun updateDisplayScale(name: String, scale: Vector3f, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Scale(scale))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "scale to ${scale.x}, ${scale.y}, ${scale.z}"
        )
        playSuccessSound(source)
        return true
    }

    fun resetDisplayScale(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Scale(Vector3f(1f)))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "scale reset to default")
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBillboard(name: String, mode: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val newMode = try {
            BillboardMode.valueOf(mode.uppercase())
        } catch (e: IllegalArgumentException) {
            Messages.sendError(source, ErrorType.INVALID_BILLBOARD)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BillboardMode(newMode))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "billboard mode to ${newMode.name.lowercase()}"
        )
        playSuccessSound(source)
        return true
    }

    fun resetDisplayBillboard(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BillboardMode(BillboardMode.CENTER))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "billboard mode reset to center")
        playSuccessSound(source)
        return true
    }

    fun updateDisplayRotation(
        name: String,
        pitch: Float,
        yaw: Float,
        roll: Float,
        source: ServerCommandSource
    ): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Rotation(Vector3f(pitch, yaw, roll)))
        Messages.sendFeedback(source, SuccessType.ROTATION_UPDATED, "pitch: $pitch, yaw: $yaw, roll: $roll")
        playSuccessSound(source)
        return true
    }

    fun resetDisplayRotation(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Rotation(Vector3f()))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "rotation reset to default")
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBackground(name: String, color: String, opacity: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        if (!color.matches(Regex("^[0-9A-Fa-f]{6}$"))) {
            Messages.sendError(source, ErrorType.INVALID_COLOR)
            playErrorSound(source)
            return false
        }

        val opacityHex = ((opacity.coerceIn(1, 100) / 100.0 * 255).toInt())
            .toString(16)
            .padStart(2, '0')
            .uppercase()

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Background("$opacityHex$color"))
        Messages.sendFeedback(source, SuccessType.BACKGROUND_UPDATED, "#$color (${opacity}% opacity)")
        playSuccessSound(source)
        return true
    }

    fun resetDisplayBackground(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Background(null))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED)
        playSuccessSound(source)
        return true
    }

    fun updateDisplayTextOpacity(name: String, opacity: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        if (opacity !in 1..100) {
            Messages.sendError(source, ErrorType.INVALID_TEXT_OPACITY)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextOpacity(opacity))
        Messages.sendFeedback(source, SuccessType.OPACITY_UPDATED, opacity.toString())
        playSuccessSound(source)
        return true
    }

    fun updateDisplayShadow(name: String, shadow: Boolean, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Shadow(shadow))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "shadow ${if (shadow) "enabled" else "disabled"}"
        )
        playSuccessSound(source)
        return true
    }

    fun updateDisplayAlignment(name: String, alignment: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val textAlignment = try {
            TextDisplay.TextAlignment.valueOf(alignment.uppercase())
        } catch (e: IllegalArgumentException) {
            Messages.sendError(source, ErrorType.INVALID_ALIGNMENT)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextAlignment(textAlignment))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "text alignment to ${textAlignment.name.lowercase()}"
        )
        playSuccessSound(source)
        return true
    }

    fun updateItemDisplayType(name: String, type: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.ItemDisplayType(type))
        Messages.sendFeedback(source, SuccessType.DISPLAY_UPDATED, "item display type to $type")
        playSuccessSound(source)
        return true
    }

    // Display Line Management
    fun deleteDisplay(name: String, source: ServerCommandSource): Boolean {
        val affectedHolograms = HologramConfig.getHolograms()
            .filter { (_, hologram) ->
                hologram.displays.any { it.displayId == name }
            }

        affectedHolograms.forEach { (hologramName, hologram) ->
            hologram.displays.removeAll { it.displayId == name }
            HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.RemoveLine(0))
        }

        DisplayConfig.deleteDisplay(name)
        Messages.sendFeedback(source, SuccessType.DISPLAY_DELETED, name)
        playSuccessSound(source)
        return true
    }

    fun addDisplayToHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(hologramName)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        if (!DisplayConfig.exists(displayName)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.AddLine(displayName))
        Messages.sendFeedback(source, SuccessType.LINE_ADDED, displayName)
        playSuccessSound(source)
        return true
    }

    fun removeLineFromHologram(hologramName: String, index: Int, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(hologramName)) {
            Messages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val hologram = HologramConfig.getHologram(hologramName) ?: return false
        if (index >= hologram.displays.size) {
            Messages.sendError(source, ErrorType.LINE_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.RemoveLine(index))
        Messages.sendFeedback(source, SuccessType.LINE_REMOVED, "#$index")
        playSuccessSound(source)
        return true
    }

    fun updateItemCustomModelData(name: String, customModelData: Int?, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            Messages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.CustomModelData(customModelData))
        Messages.sendFeedback(
            source,
            SuccessType.DISPLAY_UPDATED,
            "custom model data to ${customModelData ?: "none"}"
        )
        playSuccessSound(source)
        return true
    }
}