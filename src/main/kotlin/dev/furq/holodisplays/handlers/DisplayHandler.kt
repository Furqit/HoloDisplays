package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.utils.HandlerUtils.HologramProperty
import net.minecraft.entity.decoration.DisplayEntity

object DisplayHandler {
    fun updateDisplayProperty(displayId: String, property: HologramProperty) {
        val display = DisplayConfig.getDisplay(displayId) ?: return
        val updatedDisplay = updateDisplayData(display, property) ?: return

        DisplayConfig.saveDisplay(displayId, updatedDisplay)

        val affectedHolograms = HologramConfig.getHolograms()
            .filter { (_, hologram) ->
                hologram.displays.any { it.displayId == displayId }
            }

        val needsRespawn = when (property) {
            is HologramProperty.Rotation -> true
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

    private fun updateDisplayData(display: DisplayData, property: HologramProperty): DisplayData? =
        when (property) {
            is HologramProperty.Scale -> updateScale(display, property.value)
            is HologramProperty.BillboardMode -> updateBillboard(display, property.mode)
            is HologramProperty.Rotation -> updateRotation(display, property.value)
            is HologramProperty.Lines -> updateLines(display, property.value)
            is HologramProperty.ItemId -> updateItemId(display, property.value)
            is HologramProperty.BlockId -> updateBlockId(display, property.value)
            is HologramProperty.LineWidth -> updateLineWidth(display, property.value)
            is HologramProperty.Background -> updateBackground(display, property.value)
            is HologramProperty.TextOpacity -> updateTextOpacity(display, property.value)
            is HologramProperty.Shadow -> updateShadow(display, property.value)
            is HologramProperty.SeeThrough -> updateSeeThrough(display, property.value)
            is HologramProperty.TextAlignment -> updateTextAlignment(display, property.value)
            is HologramProperty.ItemDisplayType -> updateItemDisplayType(display, property.value)
            else -> null
        }

    private fun updateScale(display: DisplayData, scale: Float?) = display.copy(
        displayType = when (val type = display.displayType) {
            is DisplayData.DisplayType.Text -> type.copy(scale = scale)
            is DisplayData.DisplayType.Item -> type.copy(scale = scale)
            is DisplayData.DisplayType.Block -> type.copy(scale = scale)
        }
    )

    private fun updateBillboard(display: DisplayData, mode: DisplayEntity.BillboardMode?) = display.copy(
        displayType = when (val type = display.displayType) {
            is DisplayData.DisplayType.Text -> type.copy(billboardMode = mode)
            is DisplayData.DisplayType.Item -> type.copy(billboardMode = mode)
            is DisplayData.DisplayType.Block -> type.copy(billboardMode = mode)
        }
    )

    private fun updateRotation(display: DisplayData, rotation: HologramData.Rotation?) = display.copy(
        displayType = when (val type = display.displayType) {
            is DisplayData.DisplayType.Text -> type.copy(rotation = rotation)
            is DisplayData.DisplayType.Item -> type.copy(rotation = rotation)
            is DisplayData.DisplayType.Block -> type.copy(rotation = rotation)
        }
    )

    private fun updateLines(display: DisplayData, lines: List<String>): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(
            displayType = display.displayType.copy(lines = lines.toMutableList())
        )
    }

    private fun updateItemId(display: DisplayData, id: String): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Item) return null
        return display.copy(displayType = display.displayType.copy(id = id))
    }

    private fun updateBlockId(display: DisplayData, id: String): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Block) return null
        return display.copy(displayType = display.displayType.copy(id = id))
    }

    private fun updateLineWidth(display: DisplayData, width: Int?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(displayType = display.displayType.copy(lineWidth = width))
    }

    private fun updateBackground(display: DisplayData, background: String?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(
            displayType = display.displayType.copy(
                backgroundColor = background?.let {
                    if (it.matches(Regex("^[0-9A-Fa-f]{2}[0-9A-Fa-f]{6}$"))) it
                    else null
                }
            )
        )
    }

    private fun updateTextOpacity(display: DisplayData, opacity: Int?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(displayType = display.displayType.copy(textOpacity = opacity))
    }

    private fun updateShadow(display: DisplayData, shadow: Boolean?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(displayType = display.displayType.copy(shadow = shadow))
    }

    private fun updateSeeThrough(display: DisplayData, seeThrough: Boolean?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(displayType = display.displayType.copy(seeThrough = seeThrough))
    }

    private fun updateTextAlignment(display: DisplayData, alignment: DisplayData.TextAlignment?): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Text) return null
        return display.copy(displayType = display.displayType.copy(alignment = alignment))
    }

    private fun updateItemDisplayType(display: DisplayData, type: String): DisplayData? {
        if (display.displayType !is DisplayData.DisplayType.Item) return null
        return display.copy(displayType = display.displayType.copy(itemDisplayType = type))
    }
}