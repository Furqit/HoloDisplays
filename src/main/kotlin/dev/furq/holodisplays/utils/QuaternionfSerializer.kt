package dev.furq.holodisplays.utils

import kotlinx.serialization.KSerializer
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

    override fun deserialize(decoder: Decoder) =
        decoder.decodeSerializableValue(delegate).let { (x, y, z, w) -> Quaternionf(x, y, z, w) }
}
