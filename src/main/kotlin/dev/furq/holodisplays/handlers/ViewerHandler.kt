package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*

object ViewerHandler {
    private data class TrackedHologram(
        val hologramName: String,
        val hologramData: HologramData,
        val observers: MutableSet<UUID> = mutableSetOf(),
    )

    private val trackedHolograms = mutableMapOf<String, TrackedHologram>()

    fun createTracker(name: String, data: HologramData) {
        trackedHolograms[name] = TrackedHologram(name, data)
    }

    fun removeTracker(name: String) {
        trackedHolograms.remove(name)
    }

    fun clearAllTrackers() {
        trackedHolograms.clear()
    }

    fun addViewer(player: ServerPlayerEntity, name: String) {
        trackedHolograms[name]?.let { tracked ->
            if (tracked.observers.add(player.uuid)) {
                showHologramToPlayer(player, name, tracked.hologramData)
            }
        }
    }

    private fun removeViewer(player: ServerPlayerEntity, name: String) {
        trackedHolograms[name]?.let { tracked ->
            if (tracked.observers.remove(player.uuid)) {
                PacketHandler.destroyHologram(player, name)
            }
        }
    }

    fun isViewing(player: ServerPlayerEntity, name: String): Boolean =
        trackedHolograms[name]?.observers?.contains(player.uuid) == true

    fun clearViewers(player: ServerPlayerEntity) {
        trackedHolograms.values
            .filter { it.observers.contains(player.uuid) }
            .forEach { tracked ->
                tracked.observers.remove(player.uuid)
                PacketHandler.destroyHologram(player, tracked.hologramName)
            }
    }

    fun removeHologramFromAllViewers(name: String) {
        trackedHolograms[name]?.let { tracked ->
            HoloDisplays.SERVER?.playerManager?.playerList
                ?.filter { tracked.observers.contains(it.uuid) }
                ?.forEach { player ->
                    removeViewer(player, name)
                }
        }
    }

    fun respawnForAllObservers(name: String) {
        trackedHolograms[name]?.let { tracked ->
            HoloDisplays.SERVER?.playerManager?.playerList
                ?.filter { tracked.observers.contains(it.uuid) }
                ?.forEach { player ->
                    PacketHandler.destroyHologram(player, name)
                    showHologramToPlayer(player, name, tracked.hologramData)
                }
        }
    }

    fun updateForAllObservers(name: String) {
        trackedHolograms[name]?.let { tracked ->
            HoloDisplays.SERVER?.playerManager?.playerList
                ?.filter { tracked.observers.contains(it.uuid) }
                ?.forEach { player ->
                    updateHologramForPlayer(player, name, tracked.hologramData)
                }
        }
    }

    private fun showHologramToPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) {
        hologram.displays.forEachIndexed { index, entity ->
            DisplayConfig.getDisplay(entity.displayId)?.let { display ->
                val processedDisplay = when (display.displayType) {
                    is DisplayData.DisplayType.Text -> display.copy(
                        displayType = display.displayType.copy(
                            lines = mutableListOf(display.displayType.lines.joinToString("\n"))
                        )
                    )

                    else -> display
                }

                PacketHandler.spawnDisplayEntity(
                    player,
                    name,
                    entity,
                    processedDisplay,
                    Vec3d(
                        hologram.position.x.toDouble(),
                        hologram.position.y.toDouble(),
                        hologram.position.z.toDouble()
                    ),
                    index,
                    hologram
                )
            }
        }
    }

    private fun updateHologramForPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) {
        hologram.displays.forEachIndexed { index, entity ->
            DisplayConfig.getDisplay(entity.displayId)?.let { display ->
                val processedDisplay = when (display.displayType) {
                    is DisplayData.DisplayType.Text -> display.copy(
                        displayType = display.displayType.copy(
                            lines = mutableListOf(display.displayType.lines.joinToString("\n"))
                        )
                    )

                    else -> display
                }
                PacketHandler.updateDisplayMetadata(
                    player,
                    name,
                    entity.displayId,
                    index,
                    processedDisplay,
                    hologram
                )
            }
        }
    }

    fun updatePlayerVisibility(player: ServerPlayerEntity) {
        trackedHolograms.values.forEach { tracked ->
            val isCurrentlyViewing = tracked.observers.contains(player.uuid)
            val isInSameWorld = player.world.registryKey.value.toString() == tracked.hologramData.position.world

            if (!isInSameWorld) {
                if (isCurrentlyViewing) {
                    removeViewer(player, tracked.hologramName)
                }
                return@forEach
            }

            val shouldView = HologramHandler.isPlayerInRange(
                player,
                tracked.hologramData.position,
                tracked.hologramData.viewRange
            )

            when {
                shouldView && !isCurrentlyViewing -> addViewer(player, tracked.hologramName)
                !shouldView && isCurrentlyViewing -> removeViewer(player, tracked.hologramName)
            }
        }
    }

    fun getObserverCount(name: String): Int = trackedHolograms[name]?.observers?.size ?: 0
}