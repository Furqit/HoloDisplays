package dev.furq.holodisplays.managers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.HologramHandler.HologramProperty.*
import dev.furq.holodisplays.utils.ConditionEvaluator
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import java.util.*

object HologramManager {

    private inline fun requireHologramExists(
        name: String,
        source: ServerCommandSource,
        action: () -> Unit
    ) {
        if (HologramConfig.exists(name)) action()
        else FeedbackManager.send(source, FeedbackType.HOLOGRAM_NOT_FOUND, "name" to name)
    }

    private inline fun requireHologramNew(
        name: String,
        source: ServerCommandSource,
        action: () -> Unit
    ) {
        if (!HologramConfig.exists(name)) action()
        else FeedbackManager.send(source, FeedbackType.HOLOGRAM_EXISTS, "name" to name)
    }

    private fun createPosition(pos: Vec3d, world: String) = with(pos) {
        fun Double.r() = String.format(Locale.US, "%.3f", this).toFloat()
        HologramData.Position(world, x.r(), y.r(), z.r())
    }

    private fun updateProperty(
        name: String,
        source: ServerCommandSource,
        property: HologramHandler.HologramProperty,
        feedbackType: FeedbackType,
        vararg details: Pair<String, Any>
    ) = requireHologramExists(name, source) {
        HologramHandler.updateHologramProperty(name, property)
        FeedbackManager.send(source, feedbackType, *details)
    }

    fun createHologram(name: String, player: ServerPlayerEntity) = requireHologramNew(name, player.commandSource) {
        val defaultDisplayName = "${name}_text"
        val defaultDisplay = DisplayData(TextDisplay(mutableListOf("<gr #ffffff #008000>Hello, %player:name%</gr>")))
        DisplayConfig.saveDisplay(defaultDisplayName, defaultDisplay)

        val hologram = HologramData(
            displays = mutableListOf(HologramData.DisplayLine(defaultDisplayName)),
            position = createPosition(player.pos, player.world.registryKey.value.toString()),
            rotation = Vector3f(),
            scale = Vector3f(1f),
            billboardMode = BillboardMode.CENTER,
            updateRate = 20,
            viewRange = 16.0
        )

        HologramHandler.createHologram(name, hologram)
        FeedbackManager.send(player.commandSource, FeedbackType.HOLOGRAM_CREATED, "name" to name)
    }

    fun deleteHologram(name: String, source: ServerCommandSource) = requireHologramExists(name, source) {
        HologramHandler.deleteHologram(name)
        FeedbackManager.send(source, FeedbackType.HOLOGRAM_DELETED, "name" to name)
    }

    fun updatePosition(name: String, pos: Vec3d, worldId: String, source: ServerCommandSource) {
        val property = Position(createPosition(pos, worldId))
        updateProperty(name, source, property, FeedbackType.POSITION_UPDATED, *FeedbackManager.formatVector3f(pos.toVector3f()))
    }

    fun updateScale(name: String, scale: Vector3f?, source: ServerCommandSource) {
        requireHologramExists(name, source) {
            val newScale = scale ?: Vector3f(1f)

            if (scale != null && (newScale.x < 0.1f || newScale.y < 0.1f || newScale.z < 0.1f)) {
                FeedbackManager.send(source, FeedbackType.INVALID_SCALE)
                return
            }

            updateProperty(name, source, Scale(newScale),
                FeedbackType.SCALE_UPDATED, *FeedbackManager.formatVector3f(newScale)
            )
        }
    }

    fun updateBillboard(name: String, billboard: String?, source: ServerCommandSource) {
        requireHologramExists(name, source) {
            val newMode = if (billboard == null) {
                BillboardMode.CENTER
            } else {
                try {
                    BillboardMode.valueOf(billboard.uppercase())
                } catch (_: IllegalArgumentException) {
                    FeedbackManager.send(source, FeedbackType.INVALID_BILLBOARD)
                    return
                }
            }

            updateProperty(name, source, BillboardMode(newMode),
                FeedbackType.BILLBOARD_UPDATED, "mode" to (billboard?.lowercase() ?: "center")
            )
        }
    }

    fun updateRotation(name: String, pitch: Float?, yaw: Float?, roll: Float?, source: ServerCommandSource) {
        requireHologramExists(name, source) {
            val rotation = if (pitch == null || yaw == null || roll == null) Vector3f() else Vector3f(pitch, yaw, roll)

            if (listOf(rotation.x, rotation.y, rotation.z).any { it !in -180f..180f }) {
                FeedbackManager.send(source, FeedbackType.INVALID_ROTATION)
                return
            }

            updateProperty(name, source, Rotation(rotation),
                FeedbackType.ROTATION_UPDATED, *FeedbackManager.formatRotation(rotation.x, rotation.y, rotation.z))
        }
    }

    fun updateUpdateRate(name: String, rate: Int?, source: ServerCommandSource) {
        val newRate = rate ?: 20
        if (rate != null && newRate < 1) {
            FeedbackManager.send(source, FeedbackType.INVALID_UPDATE_RATE)
            return
        }
        updateProperty(name, source, UpdateRate(newRate), FeedbackType.HOLOGRAM_UPDATED, "detail" to "update rate set to ${newRate}t")
    }

    fun updateViewRange(name: String, range: Float?, source: ServerCommandSource) {
        val newRange = range ?: 48f
        if (range != null && newRange !in 1f..128f) {
            FeedbackManager.send(source, FeedbackType.INVALID_VIEW_RANGE)
            return
        }
        updateProperty(name, source, ViewRange(newRange.toDouble()), FeedbackType.HOLOGRAM_UPDATED, "detail" to "view range set to $newRange blocks")
    }

    fun updateCondition(name: String, condition: String?, source: ServerCommandSource) {
        if (condition != null && ConditionEvaluator.parseCondition(condition) == null) {
            FeedbackManager.send(source, FeedbackType.INVALID_CONDITION)
            return
        }
        updateProperty(name, source, ConditionalPlaceholder(condition), FeedbackType.HOLOGRAM_UPDATED, "detail" to "condition set to ${condition ?: "none"}")
    }

    fun addDisplayToHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        val hologram = HologramConfig.getHologram(hologramName)
        if (hologram!!.displays.any { it.name == displayName }) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_ALREADY_ADDED, "name" to displayName)
            return false
        }
        updateProperty(hologramName, source, AddLine(displayName), FeedbackType.DISPLAY_ADDED, "name" to displayName)
        return true
    }

    fun removeDisplayFromHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        val hologram = HologramConfig.getHologram(hologramName)
        val index = hologram!!.displays.indexOfFirst { it.name == displayName }
        if (index == -1) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return false
        }

        updateProperty(hologramName, source, RemoveLine(index), FeedbackType.DISPLAY_REMOVED, "name" to displayName)
        return true
    }


    fun updateDisplayOffset(hologramName: String, displayName: String, offset: Vector3f, source: ServerCommandSource) {
        val hologram = HologramConfig.getHologram(hologramName)
        val index = hologram!!.displays.indexOfFirst { it.name == displayName }

        if (index == -1) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }
        updateProperty(hologramName, source, LineOffset(index, offset), FeedbackType.OFFSET_UPDATED, *FeedbackManager.formatVector3f(offset))
    }
}