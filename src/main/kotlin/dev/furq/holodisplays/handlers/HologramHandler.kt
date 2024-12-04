package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.common.Offset
import dev.furq.holodisplays.utils.TextProcessor
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import dev.furq.holodisplays.data.common.Position as CommonPosition
import dev.furq.holodisplays.data.common.Rotation as CommonRotation
import dev.furq.holodisplays.data.common.Scale as CommonScale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode as CommonBillboardMode

object HologramHandler {
    sealed class HologramProperty {
        data class Scale(val value: CommonScale?) : HologramProperty()
        data class BillboardMode(val mode: CommonBillboardMode?) : HologramProperty()
        data class ViewRange(val value: Double?) : HologramProperty()
        data class UpdateRate(val value: Int?) : HologramProperty()
        data class Position(val value: CommonPosition) : HologramProperty()
        data class Rotation(val value: CommonRotation?) : HologramProperty()
        data class LineOffset(val index: Int, val offset: Offset) : HologramProperty()
        data class AddLine(val displayId: String, val offset: Offset = Offset()) : HologramProperty()
        data class RemoveLine(val index: Int) : HologramProperty()
    }

    fun init() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name, data)
        }
    }

    fun clearAll() {
        PacketHandler.clearAllHolograms()
        TextProcessor.init()
        ViewerHandler.clearAllTrackers()
    }

    fun reinitialize() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name, data)
            ViewerHandler.respawnForAllObservers(name)
        }
    }

    fun createHologram(name: String, data: HologramData) {
        HologramConfig.saveHologram(name, data)
        ViewerHandler.createTracker(name, data)
        showHologramToPlayers(name, data)
    }

    private fun showHologramToPlayers(name: String, data: HologramData) {
        getPlayersInRange(data).forEach { player ->
            ViewerHandler.addViewer(player, name)
        }
    }

    fun updateHologramProperty(name: String, property: HologramProperty) {
        val hologram = HologramConfig.getHologram(name) ?: return
        updateHologramData(hologram, property)
        HologramConfig.saveHologram(name, hologram)

        val needsRespawn = when (property) {
            is HologramProperty.Position,
            is HologramProperty.Rotation,
            is HologramProperty.LineOffset,
            is HologramProperty.AddLine,
            is HologramProperty.RemoveLine,
            -> true

            else -> false
        }

        if (needsRespawn) {
            ViewerHandler.respawnForAllObservers(name)
        } else {
            ViewerHandler.updateForAllObservers(name)
        }
    }

    private fun updateHologramData(hologram: HologramData, property: HologramProperty) {
        when (property) {
            is HologramProperty.Scale -> hologram.scale = property.value ?: hologram.scale
            is HologramProperty.BillboardMode -> hologram.billboardMode = property.mode ?: hologram.billboardMode
            is HologramProperty.ViewRange -> hologram.viewRange = property.value ?: hologram.viewRange
            is HologramProperty.UpdateRate -> hologram.updateRate = property.value ?: hologram.updateRate
            is HologramProperty.Position -> hologram.position = property.value
            is HologramProperty.Rotation -> hologram.rotation = property.value ?: hologram.rotation
            is HologramProperty.LineOffset -> updateLineOffset(hologram, property)
            is HologramProperty.AddLine -> hologram.displays.add(
                HologramData.DisplayLine(
                    property.displayId,
                    property.offset
                )
            )

            is HologramProperty.RemoveLine -> {
                if (property.index in hologram.displays.indices) {
                    hologram.displays.removeAt(property.index)
                }
            }
        }
    }

    private fun updateLineOffset(hologram: HologramData, property: HologramProperty.LineOffset) {
        if (property.index in hologram.displays.indices) {
            hologram.displays[property.index] = hologram.displays[property.index].copy(
                offset = property.offset
            )
        }
    }

    fun deleteHologram(name: String) {
        ViewerHandler.removeHologramFromAllViewers(name)
        HologramConfig.deleteHologram(name)
        ViewerHandler.removeTracker(name)
    }

    private fun getPlayersInRange(data: HologramData): List<ServerPlayerEntity> {
        return getWorld(data.position).players
            ?.filterIsInstance<ServerPlayerEntity>()
            ?.filter { isPlayerInRange(it, data.position, data.viewRange) }
            ?: emptyList()
    }

    fun isPlayerInRange(
        player: ServerPlayerEntity,
        position: CommonPosition,
        viewRange: Double,
    ): Boolean {
        if (player.world == getWorld(position)) {
            return player.pos.squaredDistanceTo(
                position.x.toDouble(),
                position.y.toDouble(),
                position.z.toDouble()
            ) <= viewRange * viewRange
        }
        return false
    }

    private fun getWorld(position: CommonPosition): World {
        val worldId = Identifier.tryParse(position.world)!!
        return HoloDisplays.SERVER!!.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId))!!
    }
}