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
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.joml.Vector3f

object Utils {
    fun createHologram(name: String, player: ServerPlayerEntity) {
        if (HologramConfig.exists(name)) {
            ErrorMessages.sendError(player.commandSource, ErrorType.HOLOGRAM_EXISTS)
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
        val worldId = player.world.registryKey.value.toString()
        val hologram = HologramData(
            displays = mutableListOf(HologramData.DisplayLine(defaultDisplayName)),
            position = Vector3f(
                String.format("%.3f", pos.x).toFloat(),
                String.format("%.3f", pos.y).toFloat(),
                String.format("%.3f", pos.z).toFloat()
            ),
            world = worldId,
            rotation = Vector3f(),
            scale = Vector3f(1f),
            billboardMode = BillboardMode.CENTER,
            updateRate = 20,
            viewRange = 16.0,
        )

        HologramHandler.createHologram(name, hologram)
        playSuccessSound(player.commandSource)
    }

    fun updateHologramScale(name: String, scale: Vector3f, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.Scale(scale))
        playSuccessSound(source)
        return true
    }

    fun updateHologramBillboard(name: String, billboard: String, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val newMode = try {
            BillboardMode.valueOf(billboard.uppercase())
        } catch (e: IllegalArgumentException) {
            ErrorMessages.sendError(source, ErrorType.INVALID_BILLBOARD)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.BillboardMode(newMode))
        playSuccessSound(source)
        return true
    }

    fun updateHologramUpdateRate(name: String, rate: Int, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.UpdateRate(rate))
        playSuccessSound(source)
        return true
    }

    fun updateHologramViewRange(name: String, range: Float, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(name, HologramHandler.HologramProperty.ViewRange(range.toDouble()))
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
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(
            name,
            HologramHandler.HologramProperty.Rotation(Vector3f(pitch, yaw, roll))
        )
        playSuccessSound(source)
        return true
    }

    fun deleteHologram(name: String, source: ServerCommandSource): Boolean {
        HologramHandler.deleteHologram(name)
        playSuccessSound(source)
        return true
    }

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
        playSuccessSound(source)
        return true
    }

    fun updateDisplayText(name: String, text: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Lines(listOf(text)))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayLineWidth(name: String, width: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.LineWidth(width))
        playSuccessSound(source)
        return true
    }

    fun updateDisplaySeeThrough(name: String, seeThrough: Boolean, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.SeeThrough(seeThrough))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayItem(name: String, itemId: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            ErrorMessages.sendError(source, ErrorType.INVALID_ITEM)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.ItemId(fullItemId))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBlock(name: String, blockId: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            ErrorMessages.sendError(source, ErrorType.INVALID_BLOCK)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BlockId(fullBlockId))
        playSuccessSound(source)
        return true
    }

    fun createTextDisplay(name: String, text: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = TextDisplay(
                lines = mutableListOf(text)
            )
        )

        DisplayConfig.saveDisplay(name, display)
        playSuccessSound(source)
        return true
    }

    fun createItemDisplay(name: String, itemId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            ErrorMessages.sendError(source, ErrorType.INVALID_ITEM)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = ItemDisplay(
                id = fullItemId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        playSuccessSound(source)
        return true
    }

    fun createBlockDisplay(name: String, blockId: String, source: ServerCommandSource): Boolean {
        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(source)
            return false
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            ErrorMessages.sendError(source, ErrorType.INVALID_BLOCK)
            playErrorSound(source)
            return false
        }

        val display = DisplayData(
            display = BlockDisplay(
                id = fullBlockId
            )
        )

        DisplayConfig.saveDisplay(name, display)
        playSuccessSound(source)
        return true
    }

    fun addDisplayToHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        if (!HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.AddLine(displayName))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayScale(name: String, scale: Vector3f, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Scale(scale))
        playSuccessSound(source)
        return true
    }

    fun resetDisplayScale(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Scale(Vector3f(1f)))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBillboard(name: String, mode: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val newMode = try {
            BillboardMode.valueOf(mode.uppercase())
        } catch (e: IllegalArgumentException) {
            ErrorMessages.sendError(source, ErrorType.INVALID_BILLBOARD)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BillboardMode(newMode))
        playSuccessSound(source)
        return true
    }

    fun resetDisplayBillboard(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.BillboardMode(BillboardMode.CENTER))
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
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Rotation(Vector3f(pitch, yaw, roll)))
        playSuccessSound(source)
        return true
    }

    fun resetDisplayRotation(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Rotation(Vector3f()))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayBackground(name: String, color: String, opacity: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        if (!color.matches(Regex("^[0-9A-Fa-f]{6}$"))) {
            ErrorMessages.sendError(source, ErrorType.INVALID_COLOR)
            playErrorSound(source)
            return false
        }

        val opacityHex = ((opacity.coerceIn(1, 100) / 100.0 * 255).toInt())
            .toString(16)
            .padStart(2, '0')
            .uppercase()

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Background("$opacityHex$color"))
        playSuccessSound(source)
        return true
    }

    fun resetDisplayBackground(name: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Background(null))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayTextOpacity(name: String, opacity: Int, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextOpacity(opacity))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayShadow(name: String, shadow: Boolean, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.Shadow(shadow))
        playSuccessSound(source)
        return true
    }

    fun updateDisplayAlignment(name: String, alignment: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val textAlignment = try {
            TextDisplay.TextAlignment.valueOf(alignment.uppercase())
        } catch (e: IllegalArgumentException) {
            ErrorMessages.sendError(source, ErrorType.INVALID_ALIGNMENT)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextAlignment(textAlignment))
        playSuccessSound(source)
        return true
    }

    fun updateItemDisplayType(name: String, type: String, source: ServerCommandSource): Boolean {
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.ItemDisplayType(type))
        playSuccessSound(source)
        return true
    }

    fun updateHologramPosition(
        name: String,
        position: Vector3f,
        worldId: String,
        source: ServerCommandSource
    ): Boolean {
        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(source)
            return false
        }

        val formattedPosition = Vector3f(
            String.format("%.3f", position.x).toFloat(),
            String.format("%.3f", position.y).toFloat(),
            String.format("%.3f", position.z).toFloat()
        )

        HologramHandler.updateHologramProperty(
            name,
            HologramHandler.HologramProperty.Position(formattedPosition, worldId)
        )
        playSuccessSound(source)
        return true
    }
}