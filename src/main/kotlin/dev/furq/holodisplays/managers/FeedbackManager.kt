package dev.furq.holodisplays.managers

import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import org.joml.Vector3f

object FeedbackManager {
    private const val PREFIX = "§8[§bHoloDisplays§8]§r "

    fun send(source: CommandSourceStack, type: FeedbackType, vararg params: Pair<String, Any>) {
        val message = type.format(*params)
        val formattedMessage = formatMessage(type, message)

        when {
            type.isError -> {
                source.sendFailure(formattedMessage)
                playErrorSound(source)
            }

            else -> {
                source.sendSuccess({ formattedMessage }, false)
                playSuccessSound(source)
            }
        }
    }

    private fun formatMessage(type: FeedbackType, message: String): Component {
        val color = if (type.isError) ChatFormatting.RED else ChatFormatting.GREEN
        return Component.literal(PREFIX + message).withStyle(color)
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

    private fun playSuccessSound(source: CommandSourceStack) {
        source.player?.playSoundToPlayer(
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.MASTER,
            0.5f,
            1f
        )
    }

    private fun playErrorSound(source: CommandSourceStack) {
        source.player?.playSoundToPlayer(
            SoundEvents.VILLAGER_NO,
            SoundSource.MASTER,
            0.5f,
            1f
        )
    }

    fun ServerPlayer.playSoundToPlayer(
        sound: SoundEvent?,
        category: SoundSource?,
        volume: Float,
        pitch: Float
    ) {
        if (sound == null) return
        this.connection.send(
            ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                category ?: SoundSource.MASTER,
                this.x,
                this.y,
                this.z,
                volume,
                pitch,
                this.random.nextLong()
            )
        )
    }
}
