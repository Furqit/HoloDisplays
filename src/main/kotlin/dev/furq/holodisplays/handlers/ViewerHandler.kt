package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.utils.ConditionEvaluator
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*

object ViewerHandler {
    private data class TrackedHologram(
        val hologramData: HologramData,
        val observers: MutableSet<UUID> = mutableSetOf(),
    )

    private val trackedHolograms = mutableMapOf<String, TrackedHologram>()
    private val playerManager get() = HoloDisplays.SERVER?.playerManager
    private fun getPlayer(uuid: UUID): ServerPlayerEntity? = playerManager?.getPlayer(uuid)
    private fun onlinePlayers(): List<ServerPlayerEntity> = playerManager?.playerList ?: emptyList()
    fun isViewing(player: ServerPlayerEntity, name: String): Boolean = trackedHolograms[name]?.observers?.contains(player.uuid) == true

    fun createTracker(name: String, data: HologramData) {
        trackedHolograms[name] = TrackedHologram(data)
    }

    fun removeTracker(name: String) {
        trackedHolograms.remove(name)
    }

    fun clearTrackers() {
        trackedHolograms.clear()
    }

    fun addViewer(player: ServerPlayerEntity, name: String) = safeCall {
        val tracked = trackedHolograms[name] ?: throw HologramException("Hologram $name not found")
        if (tracked.observers.add(player.uuid)) {
            showHologramToPlayer(player, name, tracked.hologramData)
        }
    }

    private fun removeViewer(player: ServerPlayerEntity, name: String) {
        trackedHolograms[name]?.let { tracked ->
            if (tracked.observers.remove(player.uuid)) {
                PacketHandler.destroyDisplayEntity(player, name)
            }
        }
    }

    fun clearViewers(player: ServerPlayerEntity) {
        trackedHolograms.filterValues { it.observers.contains(player.uuid) }
            .forEach { (name, _) ->
                removeViewer(player, name)
            }
    }

    fun removeHologramFromAllViewers(name: String) {
        trackedHolograms[name]?.let { tracked ->
            val observerIds = tracked.observers.toList()
            observerIds.forEach { uuid ->
                getPlayer(uuid)?.let { player ->
                    removeViewer(player, name)
                }
            }
        }
    }

    fun respawnForAllObservers(name: String) {
        val hologramData = HologramConfig.getHologram(name) ?: return
        val observerIds = trackedHolograms[name]?.observers?.toList() ?: return
        observerIds.forEach { uuid ->
            getPlayer(uuid)?.let { player ->
                PacketHandler.destroyDisplayEntity(player, name)
                showHologramToPlayer(player, name, hologramData)
            }
        }
    }

    fun updateForAllObservers(name: String) = safeCall {
        val tracked = trackedHolograms[name] ?: throw HologramException("Hologram $name not found")
        val hologramData = HologramConfig.getHologram(name) ?: throw HologramException("Hologram $name not found in config")
        onlinePlayers()
            .filter { tracked.observers.contains(it.uuid) }
            .forEach { player ->
                updateHologramForPlayer(player, name, hologramData)
            }
    }

    private fun showHologramToPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) = safeCall {
        if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) return@safeCall

        hologram.displays.forEachIndexed { index, entity ->
            val display = DisplayConfig.getDisplayOrAPI(entity.displayId) ?: throw HologramException("Display ${entity.displayId} not found")
            if (!ConditionEvaluator.evaluate(display.type.conditionalPlaceholder, player)) return@forEachIndexed

            PacketHandler.spawnDisplayEntity(player, name, entity, processDisplayForPlayer(display), hologramPosition(hologram), index, hologram)
        }
    }

    private fun hologramPosition(hologram: HologramData) = Vec3d(
        hologram.position.x.toDouble(),
        hologram.position.y.toDouble(),
        hologram.position.z.toDouble()
    )

    private fun processDisplayForPlayer(display: DisplayData): DisplayData = when (val displayType = display.type) {
        is TextDisplay -> display.copy(type = displayType.copy(lines = mutableListOf(displayType.getText())))
        else -> display
    }

    private fun updateHologramForPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) {
        if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) return

        hologram.displays.forEachIndexed { index, entity ->
            DisplayConfig.getDisplayOrAPI(entity.displayId)?.let { display ->
                if (!ConditionEvaluator.evaluate(display.type.conditionalPlaceholder, player)) return@let

                PacketHandler.updateDisplayMetadata(
                    player, name, entity.displayId, index, processDisplayForPlayer(display), hologram
                )
            }
        }
    }

    fun updatePlayerVisibility(player: ServerPlayerEntity) {
        val playerWorld = player.world.registryKey.value.toString()

        trackedHolograms.forEach { (name, tracked) ->
            val hologram = tracked.hologramData
            val isCurrentlyViewing = tracked.observers.contains(player.uuid)

            if (hologram.world != playerWorld) {
                if (isCurrentlyViewing) {
                    removeViewer(player, name)
                }
                return@forEach
            }

            val shouldView = ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player) &&
                    HologramHandler.isPlayerInRange(player, hologram.world, hologram.position, hologram.viewRange)
            when {
                shouldView && !isCurrentlyViewing -> addViewer(player, name)
                !shouldView && isCurrentlyViewing -> removeViewer(player, name)
            }
        }
    }

    fun getObserverCount(name: String): Int = trackedHolograms[name]?.observers?.size ?: 0
}