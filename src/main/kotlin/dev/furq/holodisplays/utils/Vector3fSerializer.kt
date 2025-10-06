package dev.furq.holodisplays.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Vector3f

object Vector3fSerializer : KSerializer<Vector3f> {
    private val delegate = ListSerializer(Float.serializer())
    override val descriptor get() = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Vector3f) =
        encoder.encodeSerializableValue(delegate, listOf(value.x, value.y, value.z))

    override fun deserialize(decoder: Decoder) =
        decoder.decodeSerializableValue(delegate).let { (x, y, z) -> Vector3f(x, y, z) }
}
