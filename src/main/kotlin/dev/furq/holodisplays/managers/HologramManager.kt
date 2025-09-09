package dev.furq.holodisplays.managers

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.HologramHandler.HologramProperty.*
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import java.util.*

object HologramManager {
    private fun validateHologramName(name: String, source: ServerCommandSource): Boolean =
        HologramConfig.exists(name).also { exists ->
            if (exists) FeedbackManager.send(source, FeedbackType.HOLOGRAM_EXISTS, "name" to name)
        }.not()

    private fun validateHologramExists(name: String, source: ServerCommandSource): Boolean =
        HologramConfig.exists(name).also { exists ->
            if (!exists) FeedbackManager.send(source, FeedbackType.HOLOGRAM_NOT_FOUND, "name" to name)
        }

    private fun formatPosition(pos: Vec3d): Vector3f = Vector3f(
        "%.3f".format(Locale.US, pos.x).toFloat(),
        "%.3f".format(Locale.US, pos.y).toFloat(),
        "%.3f".format(Locale.US, pos.z).toFloat()
    )

    fun createHologram(name: String, player: ServerPlayerEntity) {
        if (!validateHologramName(name, player.commandSource)) return

        val defaultDisplayName = "${name}_text"
        val defaultDisplay = DisplayData(TextDisplay(mutableListOf("<gr #ffffff #008000>Hello, %player:name%</gr>")))
        DisplayConfig.saveDisplay(defaultDisplayName, defaultDisplay)

        val hologram = HologramData(
            displays = mutableListOf(HologramData.DisplayLine(defaultDisplayName)),
            position = formatPosition(player.pos),
            world = player.world.registryKey.value.toString(),
            rotation = Vector3f(),
            scale = Vector3f(1f),
            billboardMode = BillboardMode.CENTER,
            updateRate = 20,
            viewRange = 16.0,
        )

        HologramHandler.createHologram(name, hologram)
        FeedbackManager.send(player.commandSource, FeedbackType.HOLOGRAM_CREATED, "name" to name)
    }

    fun deleteHologram(name: String, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        HologramHandler.deleteHologram(name)
        FeedbackManager.send(source, FeedbackType.HOLOGRAM_DELETED, "name" to name)
    }

    fun updatePosition(name: String, pos: Vec3d, worldId: String, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        HologramHandler.updateHologramProperty(name, Position(formatPosition(pos), worldId))
        FeedbackManager.send(source, FeedbackType.POSITION_UPDATED, *FeedbackManager.formatVector3f(formatPosition(pos)))
    }

    private fun validateScale(scale: Vector3f, source: ServerCommandSource): Boolean =
        (scale.x >= 0.1f && scale.y >= 0.1f && scale.z >= 0.1f).also { valid ->
            if (!valid) FeedbackManager.send(source, FeedbackType.INVALID_SCALE)
        }

    private fun validateBillboardMode(billboard: String, source: ServerCommandSource): BillboardMode? =
        try {
            BillboardMode.valueOf(billboard.uppercase())
        } catch (_: IllegalArgumentException) {
            FeedbackManager.send(source, FeedbackType.INVALID_BILLBOARD)
            null
        }

    fun updateScale(name: String, scale: Vector3f, source: ServerCommandSource) {
        if (!validateHologramExists(name, source) || !validateScale(scale, source)) return

        HologramHandler.updateHologramProperty(name, Scale(scale))
        FeedbackManager.send(source, FeedbackType.SCALE_UPDATED, *FeedbackManager.formatVector3f(scale))
    }

    fun updateBillboard(name: String, billboard: String, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        validateBillboardMode(billboard, source)?.let { newMode ->
            HologramHandler.updateHologramProperty(name, BillboardMode(newMode))
            FeedbackManager.send(source, FeedbackType.BILLBOARD_UPDATED, "mode" to billboard.lowercase())
        }
    }

    fun updateUpdateRate(name: String, rate: Int, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        if (rate !in 1..100) {
            FeedbackManager.send(source, FeedbackType.INVALID_UPDATE_RATE)
            return
        }

        HologramHandler.updateHologramProperty(name, UpdateRate(rate))
        FeedbackManager.send(source, FeedbackType.HOLOGRAM_UPDATED, "detail" to "update rate set to ${rate}t")
    }

    fun updateViewRange(name: String, range: Float, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        if (range !in 1f..128f) {
            FeedbackManager.send(source, FeedbackType.INVALID_VIEW_RANGE)
            return
        }

        HologramHandler.updateHologramProperty(name, ViewRange(range.toDouble()))
        FeedbackManager.send(source, FeedbackType.HOLOGRAM_UPDATED, "detail" to "view range set to $range blocks")
    }

    fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        HologramHandler.updateHologramProperty(name, Rotation(Vector3f(pitch, yaw, roll)))
        FeedbackManager.send(source, FeedbackType.ROTATION_UPDATED, *FeedbackManager.formatRotation(pitch, yaw, roll))
    }

    fun updateCondition(name: String, condition: String?, source: ServerCommandSource) {
        if (!validateHologramExists(name, source)) return

        HologramHandler.updateHologramProperty(name, ConditionalPlaceholder(condition))
        FeedbackManager.send(source, FeedbackType.HOLOGRAM_UPDATED, "detail" to "condition set to ${condition ?: "none"}")
    }

    fun addDisplayToHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        if (!validateHologramExists(hologramName, source)) return false

        val hologram = HologramConfig.getHologram(hologramName)
        if (hologram!!.displays.any { it.displayId == displayName }) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_ALREADY_ADDED, "name" to displayName)
            return false
        }

        HologramHandler.updateHologramProperty(hologramName, AddLine(displayName))
        FeedbackManager.send(source, FeedbackType.DISPLAY_ADDED, "name" to displayName)
        return true
    }

    fun removeDisplayFromHologram(hologramName: String, displayName: String, source: ServerCommandSource): Boolean {
        if (!validateHologramExists(hologramName, source)) return false

        val hologram = HologramConfig.getHologram(hologramName)
        val displayIndex = hologram!!.displays.indexOfFirst { it.displayId == displayName }

        if (displayIndex == -1) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return false
        }

        HologramHandler.updateHologramProperty(hologramName, RemoveLine(displayIndex))
        FeedbackManager.send(source, FeedbackType.DISPLAY_REMOVED, "name" to displayName)
        return true
    }

    fun updateDisplayOffset(hologramName: String, displayName: String, offset: Vector3f, source: ServerCommandSource) {
        if (!validateHologramExists(hologramName, source)) return

        val hologram = HologramConfig.getHologram(hologramName)
        val displayIndex = hologram!!.displays.indexOfFirst { it.displayId == displayName }

        if (displayIndex == -1) {
            FeedbackManager.send(source, FeedbackType.DISPLAY_NOT_FOUND, "name" to displayName)
            return
        }

        HologramHandler.updateHologramProperty(hologramName, LineOffset(displayIndex, offset))
        FeedbackManager.send(source, FeedbackType.OFFSET_UPDATED, *FeedbackManager.formatVector3f(offset))
    }
}