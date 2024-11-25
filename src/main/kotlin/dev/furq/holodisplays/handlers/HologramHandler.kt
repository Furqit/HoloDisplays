package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.utils.HandlerUtils
import dev.furq.holodisplays.utils.HandlerUtils.HologramProperty
import dev.furq.holodisplays.utils.TextProcessor
import net.minecraft.server.network.ServerPlayerEntity

object HologramHandler {

    fun init() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name, data)
        }
    }

    fun clearAll() {
        PacketHandler.clearAllHolograms()
        HandlerUtils.clearWorldCache()
        TextProcessor.clearCache()
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
        HandlerUtils.getHologramWorld(data)?.players
            ?.filterIsInstance<ServerPlayerEntity>()
            ?.filter { isPlayerInRange(it, data.position, data.viewRange) }
            ?.forEach { player -> ViewerHandler.addViewer(player, name) }
    }

    fun updateHologramProperty(name: String, property: HologramProperty) {
        val hologram = HologramConfig.getHologram(name) ?: return
        updateHologramData(hologram, property)
        HologramConfig.saveHologram(name, hologram)

        val needsRespawn = when (property) {
            is HologramProperty.Position -> true
            is HologramProperty.Rotation -> true
            else -> false
        }

        if (needsRespawn) {
            ViewerHandler.respawnForAllObservers(name)
        } else {
            ViewerHandler.updateForAllObservers(name)
        }
    }

    fun deleteHologram(name: String) {
        ViewerHandler.removeHologramFromAllViewers(name)
        HologramConfig.deleteHologram(name)
        ViewerHandler.removeTracker(name)
    }

    private fun updateHologramData(hologram: HologramData, property: HologramProperty) {
        when (property) {
            is HologramProperty.Scale -> hologram.scale = property.value ?: hologram.scale
            is HologramProperty.BillboardMode -> hologram.billboardMode = property.mode ?: hologram.billboardMode
            is HologramProperty.ViewRange -> hologram.viewRange = property.value ?: hologram.viewRange
            is HologramProperty.UpdateRate -> hologram.updateRate = property.value ?: hologram.updateRate
            is HologramProperty.Position -> hologram.position = property.value
            is HologramProperty.Rotation -> hologram.rotation = property.value ?: hologram.rotation
            else -> {}
        }
    }

    fun addLine(name: String, line: HologramData.DisplayLine) {
        HologramConfig.getHologram(name)?.let { hologram ->
            hologram.displays.add(line)
            HologramConfig.saveHologram(name, hologram)
            ViewerHandler.respawnForAllObservers(name)
        }
    }

    fun removeLine(name: String, index: Int) {
        HologramConfig.getHologram(name)?.let { hologram ->
            if (index in hologram.displays.indices) {
                hologram.displays.removeAt(index)
                HologramConfig.saveHologram(name, hologram)
                ViewerHandler.respawnForAllObservers(name)
            }
        }
    }

    fun isPlayerInRange(
        player: ServerPlayerEntity,
        position: HologramData.Position,
        viewRange: Double,
    ): Boolean {
        if (player.world.registryKey.value.toString() != position.world) {
            return false
        }

        return player.pos.squaredDistanceTo(
            position.x.toDouble(),
            position.y.toDouble(),
            position.z.toDouble()
        ) <= viewRange * viewRange
    }
}