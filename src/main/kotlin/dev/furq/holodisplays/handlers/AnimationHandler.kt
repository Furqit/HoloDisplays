package dev.furq.holodisplays.handlers

import dev.furq.holodisplays.config.AnimationConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.FloatFromTo
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.TransformAnimationData
import dev.furq.holodisplays.data.VecFromTo
import dev.furq.holodisplays.data.TimingOverride
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

object AnimationHandler {
    fun processTransformDisplay(
        hologramName: String,
        hologram: HologramData,
        line: HologramData.DisplayLine,
        lineIndex: Int,
        displayData: DisplayData,
        animationName: String,
        currentTick: Int,
        viewers: List<ServerPlayer>,
        bundled: MutableMap<ServerPlayer, MutableList<Packet<in ClientGamePacketListener>>>,
    ) = safeCall {
        val animation = AnimationConfig.getTransformAnimation(animationName) ?: return@safeCall
        if (!animation.hasProperties()) return@safeCall

        val scaleMultiplier = animation.scale?.easedValue(currentTick, animation)
        val rotationDelta = animation.rotation?.easedDelta(currentTick, animation)
        val translationDelta = animation.translation?.easedDelta(currentTick, animation)

        viewers.forEach { player ->
            PacketHandler.buildTransformUpdatePacket(
                player, hologramName, line.name, lineIndex, displayData, hologram, line,
                scaleMultiplier, rotationDelta, translationDelta,
            )?.let { packet ->
                bundled.getOrPut(player) { mutableListOf() }.add(packet)
            }
        }
    }

    private fun easedProgress(currentTick: Int, interval: Int, interpolation: String, loop: Boolean): Float {
        val safeInterval = interval.coerceAtLeast(1)
        val period = if (loop) safeInterval else safeInterval * 2
        val raw = (currentTick % period).toFloat() / period
        val phase = if (loop) raw else 1 - abs(1 - 2 * raw)
        return when (interpolation.lowercase()) {
            "sine" -> 0.5f - cos(phase * PI.toFloat()) * 0.5f
            else -> phase
        }
    }

    private fun lerpVector(values: VecFromTo, eased: Float): Vector3f = Vector3f(
        lerp(values.from, values.to, 0, eased),
        lerp(values.from, values.to, 1, eased),
        lerp(values.from, values.to, 2, eased),
    )

    private fun lerp(from: List<Float>, to: List<Float>, index: Int, eased: Float): Float {
        val fromValue = from.getOrElse(index) { 0f }
        val toValue = to.getOrElse(index) { 0f }
        return fromValue + (toValue - fromValue) * eased
    }

    private fun FloatFromTo.easedValue(currentTick: Int, base: TransformAnimationData): Float =
        from + (to - from) * easedTiming(currentTick, base)

    private fun VecFromTo.easedDelta(currentTick: Int, base: TransformAnimationData): Vector3f =
        lerpVector(this, easedTiming(currentTick, base))

    private fun TimingOverride.easedTiming(currentTick: Int, base: TransformAnimationData): Float =
        easedProgress(currentTick, interval ?: base.interval, interpolation ?: base.interpolation, loop ?: base.loop)
}
