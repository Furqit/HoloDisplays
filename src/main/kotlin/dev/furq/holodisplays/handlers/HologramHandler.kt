package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.joml.Vector3f
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode as MinecraftBillboardMode

object HologramHandler {
    private val worldCache = ConcurrentHashMap<String, World>()

    sealed class HologramProperty {
        data class Scale(val value: Vector3f?) : HologramProperty()
        data class BillboardMode(val mode: MinecraftBillboardMode?) : HologramProperty()
        data class ViewRange(val value: Double?) : HologramProperty()
        data class UpdateRate(val value: Int?) : HologramProperty()
        data class Position(val position: HologramData.Position) : HologramProperty()
        data class Rotation(val value: Vector3f?) : HologramProperty()
        data class LineOffset(val index: Int, val offset: Vector3f) : HologramProperty()
        data class AddLine(val displayId: String, val offset: Vector3f = Vector3f()) : HologramProperty()
        data class RemoveLine(val index: Int) : HologramProperty()
        data class ConditionalPlaceholder(val value: String?) : HologramProperty()
    }

    fun init() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name)
            ViewerHandler.updateHologramIndex(name, data.position)
        }
    }

    fun reinitialize() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name)
            ViewerHandler.updateHologramIndex(name, data.position)
            ViewerHandler.respawnForAllObservers(name)
        }
    }

    fun createHologram(name: String, data: HologramData) = safeCall {
        if (HologramConfig.exists(name)) throw HologramException("Hologram with name $name already exists")
        HologramConfig.saveHologram(name, data)
        ViewerHandler.createTracker(name)
        ViewerHandler.updateHologramIndex(name, data.position)
        showHologramToPlayers(name, data)
    }

    private fun showHologramToPlayers(name: String, data: HologramData) {
        getPlayersInRange(data).forEach { player ->
            ViewerHandler.addViewer(player, name)
        }
    }

    fun updateHologramProperty(name: String, property: HologramProperty) = safeCall {
        val hologram = HologramConfig.getHologram(name)
            ?: throw HologramException("Hologram $name not found")

        val updatedHologram = updateHologramData(hologram, property)
        HologramConfig.saveHologram(name, updatedHologram)

        val needsRespawn = when (property) {
            is HologramProperty.Position -> {
                ViewerHandler.updateHologramIndex(name, property.position)
                true
            }
            is HologramProperty.Rotation,
            is HologramProperty.ConditionalPlaceholder,
            is HologramProperty.LineOffset,
            is HologramProperty.AddLine,
            is HologramProperty.RemoveLine -> true

            else -> false
        }

        if (needsRespawn) {
            ViewerHandler.respawnForAllObservers(name)
        } else {
            ViewerHandler.updateForAllObservers(name)
        }
    }

    private fun updateHologramData(hologram: HologramData, property: HologramProperty): HologramData = when (property) {
        is HologramProperty.Scale -> hologram.copy(scale = property.value ?: Vector3f(1f))
        is HologramProperty.BillboardMode -> hologram.copy(billboardMode = property.mode ?: MinecraftBillboardMode.CENTER)
        is HologramProperty.ViewRange -> hologram.copy(viewRange = property.value ?: 48.0)
        is HologramProperty.UpdateRate -> hologram.copy(updateRate = property.value ?: 20)
        is HologramProperty.ConditionalPlaceholder -> hologram.copy(conditionalPlaceholder = property.value)
        is HologramProperty.Position -> hologram.copy(position = property.position)
        is HologramProperty.Rotation -> hologram.copy(rotation = property.value ?: Vector3f())
        is HologramProperty.LineOffset -> updateLineOffset(hologram, property)
        is HologramProperty.AddLine -> hologram.copy(
            displays = hologram.displays + HologramData.DisplayLine(property.displayId, property.offset)
        )
        is HologramProperty.RemoveLine -> hologram.copy(
            displays = hologram.displays.filterIndexed { index, _ -> index != property.index }
        ).also {
            if (property.index !in hologram.displays.indices) {
                throw HologramException("Invalid line index: ${property.index}")
            }
        }
    }

    private fun updateLineOffset(hologram: HologramData, property: HologramProperty.LineOffset): HologramData {
        require(property.index in hologram.displays.indices) { "Invalid line index: ${property.index}" }
        return hologram.copy(
            displays = hologram.displays.mapIndexed { index, displayLine ->
                if (index == property.index) displayLine.copy(offset = property.offset) else displayLine
            }
        )
    }

    fun deleteHologram(name: String) = safeCall {
        if (!HologramConfig.exists(name)) {
            throw HologramException("Hologram $name not found")
        }
        ViewerHandler.removeHologramFromAllViewers(name)
        HologramConfig.deleteHologram(name)
        ViewerHandler.removeTracker(name)
        ViewerHandler.removeHologramIndex(name)
    }

    private fun getPlayersInRange(data: HologramData): List<ServerPlayerEntity> {
        return getWorld(data.world).players
            ?.filterIsInstance<ServerPlayerEntity>()
            ?.filter { isPlayerInRange(it, data.world, data.position.toVec3f(), data.viewRange) }
            ?: emptyList()
    }

    fun isPlayerInRange(
        player: ServerPlayerEntity,
        world: String,
        position: Vector3f,
        viewRange: Double,
    ): Boolean {
        if (player.world == getWorld(world)) {
            return player.pos.squaredDistanceTo(
                position.x.toDouble(),
                position.y.toDouble(),
                position.z.toDouble()
            ) <= viewRange * viewRange
        }
        return false
    }

    private fun getWorld(world: String): World {
        return worldCache.computeIfAbsent(world) { w ->
            val worldId = Identifier.tryParse(w)
                ?: throw HologramException("Invalid world identifier: $w")
            HoloDisplays.SERVER?.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId))
                ?: throw HologramException("World not found: $w")
        }
    }
}