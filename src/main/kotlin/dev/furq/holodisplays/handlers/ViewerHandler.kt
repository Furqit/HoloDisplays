package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.utils.ConditionEvaluator
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import java.util.*

object ViewerHandler {
    private val observers = mutableMapOf<String, MutableSet<UUID>>()
    private val hologramChunkMap = mutableMapOf<Long, MutableSet<String>>()
    private val playerManager get() = HoloDisplays.SERVER?.playerList

    private fun getPlayer(uuid: UUID): ServerPlayer? = playerManager?.getPlayer(uuid)
    fun isViewing(player: ServerPlayer, name: String): Boolean = observers[name]?.contains(player.uuid) == true
    fun createTracker(name: String) = observers.getOrPut(name) { mutableSetOf() }
    fun removeTracker(name: String) = observers.remove(name)
    fun clearTrackers() = observers.clear()
    fun getObserverCount(name: String): Int = observers[name]?.size ?: 0

    fun resetAllObservers() {
        HologramConfig.getHolograms().keys.forEach { name ->
            removeHologramFromAllViewers(name)
        }
        hologramChunkMap.clear()
    }

    fun updateHologramIndex(name: String, position: HologramData.Position) {
        removeHologramIndex(name)
        //~ if >=26.1 'asLong' -> 'pack'
        val chunkLong = ChunkPos.pack(
            position.x.toInt() shr 4,
            position.z.toInt() shr 4
        )
        hologramChunkMap.getOrPut(chunkLong) { mutableSetOf() }.add(name)
    }

    fun removeHologramIndex(name: String) {
        val iterator = hologramChunkMap.values.iterator()
        while (iterator.hasNext()) {
            val set = iterator.next()
            set.remove(name)
            if (set.isEmpty()) iterator.remove()
        }
    }

    fun addViewer(player: ServerPlayer, name: String) = safeCall {
        val hologramData = HologramConfig.getHologramOrAPI(name) ?: return@safeCall
        val observerSet = observers.getOrPut(name) { mutableSetOf() }
        if (observerSet.add(player.uuid)) {
            showHologramToPlayer(player, name, hologramData)
        }
    }

    private fun removeViewer(player: ServerPlayer, name: String) {
        observers[name]?.let { observerSet ->
            if (observerSet.remove(player.uuid)) {
                PacketHandler.destroyDisplayEntity(player, name)
            }
        }
    }

    fun clearViewers(player: ServerPlayer) {
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
        val hologramData = HologramConfig.getHologramOrAPI(name) ?: return@safeCall
        observers[name]?.mapNotNull { getPlayer(it) }?.forEach { player ->
            updateHologramForPlayer(player, name, hologramData)
        }
    }

    private fun showHologramToPlayer(player: ServerPlayer, name: String, hologram: HologramData) = safeCall {
        if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) return@safeCall

        val packets = mutableListOf<Packet<in ClientGamePacketListener>>()
        val packetConsumer: (Packet<*>) -> Unit = {
            @Suppress("UNCHECKED_CAST")
            packets.add(it as Packet<in ClientGamePacketListener>)
        }

        hologram.displays.forEachIndexed { index, entity ->
            val display = DisplayConfig.getDisplayOrAPI(entity.name) ?: return@forEachIndexed
            if (!ConditionEvaluator.evaluate(display.type.conditionalPlaceholder, player)) return@forEachIndexed

            PacketHandler.spawnDisplayEntity(player, name, entity, processDisplayForPlayer(display), hologram.position.toVec3f(), index, hologram, packetConsumer)
        }

        if (packets.isNotEmpty()) {
             player.connection.send(ClientboundBundlePacket(packets))
        }
    }

    private fun processDisplayForPlayer(display: DisplayData): DisplayData = when (val displayType = display.type) {
        is TextDisplay -> display.copy(type = displayType.copy(lines = mutableListOf(displayType.getText())))
        else -> display
    }

    private fun updateHologramForPlayer(player: ServerPlayer, name: String, hologram: HologramData) {
        if (!ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player)) return

        hologram.displays.forEachIndexed { index, entity ->
            DisplayConfig.getDisplayOrAPI(entity.name)?.let { display ->
                if (!ConditionEvaluator.evaluate(display.type.conditionalPlaceholder, player)) return@let
                PacketHandler.updateDisplayMetadata(
                    player, name, entity.name, index,
                    processDisplayForPlayer(display), hologram
                )
            }
        }
    }

    fun updatePlayerVisibility(player: ServerPlayer) {
        //~ if >=1.21.11 'location' -> 'identifier'
        val playerWorld = player.level().dimension().identifier().toString()
        val playerChunkX = player.chunkPosition().x
        val playerChunkZ = player.chunkPosition().z
        
        val viewDistance = playerManager?.viewDistance ?: 10
        
        val nearbyHolograms = mutableSetOf<String>()
        for (x in -viewDistance..viewDistance) {
            for (z in -viewDistance..viewDistance) {
                //~ if >=26.1 'asLong' -> 'pack'
                val chunkLong = ChunkPos.pack(playerChunkX + x, playerChunkZ + z)
                hologramChunkMap[chunkLong]?.let { nearbyHolograms.addAll(it) }
            }
        }

        val potentialHolograms = nearbyHolograms + (observers.entries.filter { it.value.contains(player.uuid) }.map { it.key })

        potentialHolograms.forEach { name ->
            val hologram = HologramConfig.getHologramOrAPI(name) ?: return@forEach
            val isCurrentlyViewing = isViewing(player, name)

            if (hologram.world != playerWorld) {
                if (isCurrentlyViewing) {
                    removeViewer(player, name)
                }
                return@forEach
            }

            val shouldView = ConditionEvaluator.evaluate(hologram.conditionalPlaceholder, player) &&
                    HologramHandler.isPlayerInRange(player, hologram.world, hologram.position.toVec3f(), hologram.viewRange)

            when {
                shouldView && !isCurrentlyViewing -> addViewer(player, name)
                !shouldView && isCurrentlyViewing -> removeViewer(player, name)
            }
        }
    }
}