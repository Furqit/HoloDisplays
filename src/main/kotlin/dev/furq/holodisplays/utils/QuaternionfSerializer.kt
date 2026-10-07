package dev.furq.holodisplays.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Quaternionf

object QuaternionfSerializer : KSerializer<Quaternionf> {
    private val delegate = ListSerializer(Float.serializer())
    override val descriptor get() = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Quaternionf) =
        encoder.encodeSerializableValue(delegate, listOf(value.x, value.y, value.z, value.w))

    override fun deserialize(decoder: Decoder): Quaternionf {
        val components = decoder.decodeSerializableValue(delegate)
        if (components.size != 4) {
            throw SerializationException("Expected 4 floats for Quaternionf, got ${components.size}")
        }
        return Quaternionf(components[0], components[1], components[2], components[3])
    }
}
