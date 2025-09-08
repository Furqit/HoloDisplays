package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.HoloDisplays
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.joml.Vector3f
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode as MinecraftBillboardMode

object HologramHandler {
    sealed class HologramProperty {
        data class Scale(val value: Vector3f?) : HologramProperty()
        data class BillboardMode(val mode: MinecraftBillboardMode?) : HologramProperty()
        data class ViewRange(val value: Double?) : HologramProperty()
        data class UpdateRate(val value: Int?) : HologramProperty()
        data class Position(val position: Vector3f, val world: String) : HologramProperty()
        data class Rotation(val value: Vector3f?) : HologramProperty()
        data class LineOffset(val index: Int, val offset: Vector3f) : HologramProperty()
        data class AddLine(val displayId: String, val offset: Vector3f = Vector3f()) : HologramProperty()
        data class RemoveLine(val index: Int) : HologramProperty()
        data class ConditionalPlaceholder(val value: String?) : HologramProperty()
    }

    fun init() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name, data)
        }
    }

    fun reinitialize() {
        HologramConfig.getHolograms().forEach { (name, data) ->
            ViewerHandler.createTracker(name, data)
            ViewerHandler.respawnForAllObservers(name)
        }
    }

    fun createHologram(name: String, data: HologramData) = ErrorHandler.withCatch {
        if (HologramConfig.exists(name)) {
            throw HologramException("Hologram with name $name already exists")
        }
        HologramConfig.saveHologram(name, data)
        ViewerHandler.createTracker(name, data)
        showHologramToPlayers(name, data)
    }

    private fun showHologramToPlayers(name: String, data: HologramData) {
        getPlayersInRange(data).forEach { player ->
            ViewerHandler.addViewer(player, name)
        }
    }

    fun updateHologramProperty(name: String, property: HologramProperty) = ErrorHandler.withCatch {
        val hologram = HologramConfig.getHologram(name)
            ?: throw HologramException("Hologram $name not found")

        updateHologramData(hologram, property)
        HologramConfig.saveHologram(name, hologram)

        val needsRespawn = when (property) {
            is HologramProperty.Position,
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

    private fun updateHologramData(hologram: HologramData, property: HologramProperty) {
        when (property) {
            is HologramProperty.Scale -> hologram.scale = property.value ?: Vector3f(1f)
            is HologramProperty.BillboardMode -> hologram.billboardMode = property.mode ?: MinecraftBillboardMode.CENTER
            is HologramProperty.ViewRange -> hologram.viewRange = property.value ?: 48.0
            is HologramProperty.UpdateRate -> hologram.updateRate = property.value ?: 20
            is HologramProperty.ConditionalPlaceholder -> hologram.conditionalPlaceholder = property.value
            is HologramProperty.Position -> {
                hologram.position = property.position
                hologram.world = property.world
            }

            is HologramProperty.Rotation -> hologram.rotation = property.value ?: Vector3f()
            is HologramProperty.LineOffset -> {
                if (property.index !in hologram.displays.indices) {
                    throw HologramException("Invalid line index: ${property.index}")
                }
                updateLineOffset(hologram, property)
            }

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

    fun deleteHologram(name: String) = ErrorHandler.withCatch {
        if (!HologramConfig.exists(name)) {
            throw HologramException("Hologram $name not found")
        }
        ViewerHandler.removeHologramFromAllViewers(name)
        HologramConfig.deleteHologram(name)
        ViewerHandler.removeTracker(name)
    }

    private fun getPlayersInRange(data: HologramData): List<ServerPlayerEntity> {
        return getWorld(data.world).players
            ?.filterIsInstance<ServerPlayerEntity>()
            ?.filter { isPlayerInRange(it, data.world, data.position, data.viewRange) }
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

    private val worldCache = ConcurrentHashMap<String, World>()

    private fun getWorld(world: String): World {
        return worldCache.computeIfAbsent(world) { w ->
            val worldId = Identifier.tryParse(w)
                ?: throw HologramException("Invalid world identifier: $w")

            HoloDisplays.SERVER?.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId))
                ?: throw HologramException("World not found: $w")
        }
    }
}