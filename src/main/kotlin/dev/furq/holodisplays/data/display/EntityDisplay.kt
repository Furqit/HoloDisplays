@file:UseSerializers(Vector3fSerializer::class)

package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.EntityPose
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

@Serializable
data class EntityDisplay(
    val id: String,
    override val rotation: Vector3f? = null,
    override val scale: Vector3f? = null,
    val glow: Boolean? = null,
    val pose: EntityPose? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {
    override val billboardMode: Nothing? = null

    class Builder : BaseDisplay.Builder<EntityDisplay> {
        var id: String = ""
        override var rotation: Vector3f? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        var glow: Boolean? = null
        var pose: EntityPose? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = EntityDisplay(id, rotation, scale, glow, pose, conditionalPlaceholder)
    }
}