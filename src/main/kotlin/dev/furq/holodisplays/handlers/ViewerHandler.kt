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
    private val observers = mutableMapOf<String, MutableSet<UUID>>()
    private val playerManager get() = HoloDisplays.SERVER?.playerManager

    private fun getPlayer(uuid: UUID): ServerPlayerEntity? = playerManager?.getPlayer(uuid)
    fun isViewing(player: ServerPlayerEntity, name: String): Boolean = observers[name]?.contains(player.uuid) == true
    fun createTracker(name: String) = observers.getOrPut(name) { mutableSetOf() }
    fun removeTracker(name: String) = observers.remove(name)
    fun clearTrackers() = observers.clear()
    fun getObserverCount(name: String): Int = observers[name]?.size ?: 0

    fun resetAllObservers() {
        HologramConfig.getHolograms().keys.forEach { name ->
            removeHologramFromAllViewers(name)
        }
    }

    fun addViewer(player: ServerPlayerEntity, name: String) = safeCall {
        val hologramData = HologramConfig.getHologramOrAPI(name) ?: throw HologramException("Hologram $name not found")
        val observerSet = observers.getOrPut(name) { mutableSetOf() }
        if (observerSet.add(player.uuid)) {
            showHologramToPlayer(player, name, hologramData)
        }
    }

    private fun removeViewer(player: ServerPlayerEntity, name: String) {
        observers[name]?.let { observerSet ->
            if (observerSet.remove(player.uuid)) {
                PacketHandler.destroyDisplayEntity(player, name)
            }
        }
    }

    fun clearViewers(player: ServerPlayerEntity) {
        observers.keys.forEach { name -> removeViewer(player, name) }
    }

    fun removeHologramFromAllViewers(name: String) {
        observers[name]?.toList()?.forEach { uuid ->
            getPlayer(uuid)?.let { player ->
                removeViewer(player, name)
            }
        }
    }

    fun respawnForAllObservers(name: String) {
        val hologramData = HologramConfig.getHologramOrAPI(name) ?: return
        observers[name]?.toList()?.forEach { uuid ->
            getPlayer(uuid)?.let { player ->
                PacketHandler.destroyDisplayEntity(player, name)
                showHologramToPlayer(player, name, hologramData)
            }
        }
    }

    fun updateForAllObservers(name: String) = safeCall {
        val hologramData = HologramConfig.getHologramOrAPI(name) ?: throw HologramException("Hologram $name not found")
        observers[name]?.mapNotNull { getPlayer(it) }?.forEach { player ->
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
                    player, name, entity.displayId, index,
                    processDisplayForPlayer(display), hologram
                )
            }
        }
    }

    fun updatePlayerVisibility(player: ServerPlayerEntity) {
        val playerWorld = player.world.registryKey.value.toString()

        observers.keys.forEach { name ->
            val hologram = HologramConfig.getHologramOrAPI(name) ?: return@forEach
            val isCurrentlyViewing = observers[name]?.contains(player.uuid) == true

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
}