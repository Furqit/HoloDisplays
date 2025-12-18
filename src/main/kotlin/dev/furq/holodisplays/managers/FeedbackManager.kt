package dev.furq.holodisplays.managers

import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object FeedbackManager {
    private const val PREFIX = "§8[§bHoloDisplays§8]§r "

    fun send(source: ServerCommandSource, type: FeedbackType, vararg params: Pair<String, Any>) {
        val message = type.format(*params)
        val formattedMessage = formatMessage(type, message)

        when {
            type.isError -> {
                source.sendError(formattedMessage)
                playErrorSound(source)
            }

            else -> {
                source.sendFeedback({ formattedMessage }, false)
                playSuccessSound(source)
            }
        }
    }

    private fun formatMessage(type: FeedbackType, message: String): Text {
        val color = if (type.isError) Formatting.RED else Formatting.GREEN
        return Text.literal(PREFIX + message).formatted(color)
    }

    fun formatVector3f(vector: Vector3f): Array<Pair<String, Any>> = arrayOf(
        "x" to "%.2f".format(vector.x),
        "y" to "%.2f".format(vector.y),
        "z" to "%.2f".format(vector.z)
    )

    fun formatRotation(pitch: Float, yaw: Float, roll: Float): Array<Pair<String, Any>> = arrayOf(
        "pitch" to "%.2f".format(pitch),
        "yaw" to "%.2f".format(yaw),
        "roll" to "%.2f".format(roll)
    )

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

    //? if >=1.21.11 {
    fun ServerPlayerEntity.playSoundToPlayer(
        sound: SoundEvent?,
        category: SoundCategory?,
        volume: Float,
        pitch: Float
    ) {
        this.networkHandler.sendPacket(
            PlaySoundS2CPacket(
                Registries.SOUND_EVENT.getEntry(sound),
                category,
                this.x,
                this.y,
                this.z,
                volume,
                pitch,
                this.random.nextLong()
            )
        )
    }
    //?}
}
