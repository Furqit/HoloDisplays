package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.utils.ConditionEvaluator
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

    fun createTracker(name: String, data: HologramData) = ErrorHandler.withCatch {
        trackedHolograms[name] = TrackedHologram(name, data)
    }

    fun removeTracker(name: String) {
        trackedHolograms.remove(name)
    }

    fun resetHologramObservers() {
        trackedHolograms.clear()
    }

    fun addViewer(player: ServerPlayerEntity, name: String) = ErrorHandler.withCatch {
        val tracked = trackedHolograms[name] ?: throw HologramException("Hologram $name not found")
        if (tracked.observers.add(player.uuid)) {
            showHologramToPlayer(player, name, tracked.hologramData)
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

    fun updateForAllObservers(name: String) = ErrorHandler.withCatch {
        val tracked = trackedHolograms[name] ?: throw HologramException("Hologram $name not found")
        HoloDisplays.SERVER?.playerManager?.playerList
            ?.filter { tracked.observers.contains(it.uuid) }
            ?.forEach { player ->
                updateHologramForPlayer(player, name, tracked.hologramData)
            }
    }

    private fun showHologramToPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) =
        ErrorHandler.withCatch {
            if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) {
                return@withCatch
            }

            hologram.displays.forEachIndexed { index, entity ->
                val display = DisplayConfig.getDisplayOrAPI(entity.displayId)
                    ?: throw HologramException("Display ${entity.displayId} not found")

                if (!ConditionEvaluator.evaluate(display.display.conditionalPlaceholder, player)) {
                    return@forEachIndexed
                }

                val processedDisplay = when (val displayType = display.display) {
                    is TextDisplay -> display.copy(
                        display = displayType.copy(
                            lines = mutableListOf(displayType.lines.joinToString("\n"))
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

    private fun updateHologramForPlayer(player: ServerPlayerEntity, name: String, hologram: HologramData) {
        if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) {
            return
        }

        hologram.displays.forEachIndexed { index, entity ->
            DisplayConfig.getDisplayOrAPI(entity.displayId)?.let { display ->
                if (!ConditionEvaluator.evaluate(display.display.conditionalPlaceholder, player)) {
                    return@let
                }

                val processedDisplay = when (val displayType = display.display) {
                    is TextDisplay -> display.copy(
                        display = displayType.copy(
                            lines = mutableListOf(displayType.lines.joinToString("\n"))
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
        val playerWorld = player.world.registryKey.value.toString()

        trackedHolograms.values.forEach { tracked ->
            val hologram = tracked.hologramData
            val isCurrentlyViewing = tracked.observers.contains(player.uuid)

            if (hologram.world != playerWorld) {
                if (isCurrentlyViewing) {
                    removeViewer(player, tracked.hologramName)
                }
                return@forEach
            }

            val shouldView =
                ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player) &&
                        HologramHandler.isPlayerInRange(player, hologram.world, hologram.position, hologram.viewRange)

            when {
                shouldView && !isCurrentlyViewing -> addViewer(player, tracked.hologramName)
                !shouldView && isCurrentlyViewing -> removeViewer(player, tracked.hologramName)
            }
        }
    }


    fun getObserverCount(name: String): Int = trackedHolograms[name]?.observers?.size ?: 0
}