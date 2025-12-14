@file:UseSerializers(Vector3fSerializer::class, QuaternionfSerializer::class)

package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.utils.QuaternionfSerializer
import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Quaternionf
import org.joml.Vector3f

@Serializable
data class BlockDisplay(
    val id: String,
    val properties: Map<String, String> = emptyMap(),
    override val rotation: Vector3f? = null,
    override val leftRotation: Quaternionf? = null,
    override val rightRotation: Quaternionf? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<BlockDisplay> {
        var id: String = ""
        var properties: Map<String, String> = emptyMap()
        override var rotation: Vector3f? = null
        override var leftRotation: Quaternionf? = null
        override var rightRotation: Quaternionf? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = BlockDisplay(id, properties, rotation, leftRotation, rightRotation, scale, billboardMode, conditionalPlaceholder)
    }
}