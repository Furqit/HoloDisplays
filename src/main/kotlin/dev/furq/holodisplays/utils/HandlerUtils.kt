package dev.furq.holodisplays.utils

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World

object HandlerUtils {
    private val worldCache = mutableMapOf<String, World?>()

    sealed class HologramProperty {
        data class Scale(val value: Float?) : HologramProperty()
        data class BillboardMode(val mode: DisplayEntity.BillboardMode?) : HologramProperty()
        data class ViewRange(val value: Double?) : HologramProperty()
        data class UpdateRate(val value: Int?) : HologramProperty()
        data class Position(val value: HologramData.Position) : HologramProperty()
        data class Rotation(val value: HologramData.Rotation?) : HologramProperty()

        data class Lines(val value: List<String>) : HologramProperty()
        data class ItemId(val value: String) : HologramProperty()
        data class BlockId(val value: String) : HologramProperty()
        data class LineWidth(val value: Int?) : HologramProperty()
        data class Background(val value: String?) : HologramProperty()
        data class TextOpacity(val value: Int?) : HologramProperty()
        data class Shadow(val value: Boolean?) : HologramProperty()
        data class SeeThrough(val value: Boolean?) : HologramProperty()
        data class TextAlignment(val value: DisplayData.TextAlignment?) : HologramProperty()
        data class ItemDisplayType(val value: String) : HologramProperty()
    }

    fun getHologramWorld(hologram: HologramData): World? {
        return worldCache.getOrPut(hologram.position.world) {
            val server = HoloDisplays.SERVER ?: return null
            val worldId = Identifier.tryParse(hologram.position.world) ?: return null
            server.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId))
        }
    }

    fun clearWorldCache() {
        worldCache.clear()
    }
}