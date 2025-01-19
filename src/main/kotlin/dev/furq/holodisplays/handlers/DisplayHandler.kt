package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import org.joml.Vector3f
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode as MinecraftBillboardMode

object DisplayHandler {
    sealed interface DisplayProperty {
        data class Scale(val value: Vector3f) : DisplayProperty
        data class BillboardMode(val mode: MinecraftBillboardMode) : DisplayProperty
        data class Rotation(val value: Vector3f) : DisplayProperty
        data class Lines(val value: List<String>) : DisplayProperty
        data class ItemId(val value: String) : DisplayProperty
        data class BlockId(val value: String) : DisplayProperty
        data class LineWidth(val value: Int?) : DisplayProperty
        data class Background(val value: String?) : DisplayProperty
        data class TextOpacity(val value: Int?) : DisplayProperty
        data class Shadow(val value: Boolean?) : DisplayProperty
        data class SeeThrough(val value: Boolean?) : DisplayProperty
        data class TextAlignment(val value: TextDisplay.TextAlignment?) : DisplayProperty
        data class ItemDisplayType(val value: String) : DisplayProperty
        data class CustomModelData(val value: Int?) : DisplayProperty
    }

    fun updateDisplayProperty(displayId: String, property: DisplayProperty) = ErrorHandler.withCatch {
        val display = DisplayConfig.getDisplay(displayId)
            ?: throw DisplayException("Display $displayId not found")

        val updatedDisplay = updateDisplayData(display, property)
            ?: throw DisplayException("Failed to update display data")

        DisplayConfig.saveDisplay(displayId, updatedDisplay)

        val affectedHolograms = HologramConfig.getHolograms()
            .filter { (_, hologram) ->
                hologram.displays.any { it.displayId == displayId }
            }

        val needsRespawn = when (property) {
            is DisplayProperty.Rotation -> true
            else -> false
        }

        affectedHolograms.forEach { (name, _) ->
            if (needsRespawn) {
                ViewerHandler.respawnForAllObservers(name)
            } else {
                ViewerHandler.updateForAllObservers(name)
            }
        }
    }

    private fun updateDisplayData(display: DisplayData, property: DisplayProperty): DisplayData? {
        val updatedDisplay = when (val currentDisplay = display.display) {
            is TextDisplay -> updateTextDisplay(currentDisplay, property)
            is ItemDisplay -> updateItemDisplay(currentDisplay, property)
            is BlockDisplay -> updateBlockDisplay(currentDisplay, property)
            else -> null
        } ?: return null

        return DisplayData(updatedDisplay)
    }

    private fun updateTextDisplay(display: TextDisplay, property: DisplayProperty): TextDisplay? {
        return when (property) {
            is DisplayProperty.Scale -> display.copy(scale = property.value)
            is DisplayProperty.BillboardMode -> display.copy(billboardMode = property.mode)
            is DisplayProperty.Rotation -> display.copy(rotation = property.value)
            is DisplayProperty.Lines -> display.copy(lines = property.value.toMutableList())
            is DisplayProperty.LineWidth -> display.copy(lineWidth = property.value)
            is DisplayProperty.Background -> display.copy(backgroundColor = property.value)
            is DisplayProperty.TextOpacity -> display.copy(textOpacity = property.value)
            is DisplayProperty.Shadow -> display.copy(shadow = property.value)
            is DisplayProperty.SeeThrough -> display.copy(seeThrough = property.value)
            is DisplayProperty.TextAlignment -> display.copy(alignment = property.value)
            else -> null
        }
    }

    private fun updateItemDisplay(display: ItemDisplay, property: DisplayProperty): ItemDisplay? {
        return when (property) {
            is DisplayProperty.Scale -> display.copy(scale = property.value)
            is DisplayProperty.BillboardMode -> display.copy(billboardMode = property.mode)
            is DisplayProperty.Rotation -> display.copy(rotation = property.value)
            is DisplayProperty.ItemId -> display.copy(id = property.value)
            is DisplayProperty.ItemDisplayType -> display.copy(itemDisplayType = property.value)
            is DisplayProperty.CustomModelData -> display.copy(customModelData = property.value)
            else -> null
        }
    }

    private fun updateBlockDisplay(display: BlockDisplay, property: DisplayProperty): BlockDisplay? {
        return when (property) {
            is DisplayProperty.Scale -> display.copy(scale = property.value)
            is DisplayProperty.BillboardMode -> display.copy(billboardMode = property.mode)
            is DisplayProperty.Rotation -> display.copy(rotation = property.value)
            is DisplayProperty.BlockId -> display.copy(id = property.value)
            else -> null
        }
    }
}