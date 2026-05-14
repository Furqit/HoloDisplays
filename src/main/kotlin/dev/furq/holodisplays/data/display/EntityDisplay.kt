@file:UseSerializers(Vector3fSerializer::class, QuaternionfSerializer::class)

package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.utils.QuaternionfSerializer
import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.world.entity.Display.BillboardConstraints
import net.minecraft.world.entity.Pose
import org.joml.Quaternionf
import org.joml.Vector3f

@Serializable
data class EntityDisplay(
    val id: String,
    override val rotation: Vector3f? = null,
    override val leftRotation: Quaternionf? = null,
    override val rightRotation: Quaternionf? = null,
    override val scale: Vector3f? = null,
    val glow: Boolean? = null,
    val pose: Pose? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {
    override val billboardMode: Nothing? = null

    class Builder : BaseDisplay.Builder<EntityDisplay> {
        var id: String = ""
        override var rotation: Vector3f? = null
        override var leftRotation: Quaternionf? = null
        override var rightRotation: Quaternionf? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardConstraints? = null
        var glow: Boolean? = null
        var pose: Pose? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = EntityDisplay(id, rotation, leftRotation, rightRotation, scale, glow, pose, conditionalPlaceholder)
    }
}