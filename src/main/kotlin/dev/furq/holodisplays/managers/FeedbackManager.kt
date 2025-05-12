package dev.furq.holodisplays.managers

import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object FeedbackManager {
    private const val PREFIX = "§8[§bHoloDisplays§8]§r "

    fun send(source: ServerCommandSource, type: FeedbackType, vararg params: Pair<String, Any>) {
        val message = type.format(*params)
        val text = Text.literal(PREFIX + message).formatted(
            if (type.isError) Formatting.RED else Formatting.GREEN
        )

        if (type.isError) {
            source.sendError(text)
            playErrorSound(source)
        } else {
            source.sendFeedback({ text }, false)
            playSuccessSound(source)
        }
    }

    fun formatVector3f(vector: Vector3f): Array<Pair<String, String>> = listOf(
        "x" to String.format("%.2f", vector.x),
        "y" to String.format("%.2f", vector.y),
        "z" to String.format("%.2f", vector.z)
    ).toTypedArray()

    fun formatRotation(pitch: Float, yaw: Float, roll: Float): Array<Pair<String, String>> = listOf(
        "pitch" to String.format("%.2f", pitch),
        "yaw" to String.format("%.2f", yaw),
        "roll" to String.format("%.2f", roll)
    ).toTypedArray()

    private fun playSuccessSound(source: ServerCommandSource) {
        source.player?.playSoundToPlayer(
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.MASTER,
            0.5f,
            1f
        )
    }

    private fun playErrorSound(source: ServerCommandSource) {
        source.player?.playSoundToPlayer(
            SoundEvents.ENTITY_VILLAGER_NO,
            SoundCategory.MASTER,
            0.5f,
            1f
        )
    }
}