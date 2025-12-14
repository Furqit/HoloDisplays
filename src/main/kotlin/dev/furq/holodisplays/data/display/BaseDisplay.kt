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
abstract class BaseDisplay {
    abstract val scale: Vector3f?
    abstract val rotation: Vector3f?
    abstract val leftRotation: Quaternionf?
    abstract val rightRotation: Quaternionf?
    abstract val billboardMode: BillboardMode?
    abstract val conditionalPlaceholder: String?

    interface Builder<T : BaseDisplay> {
        var scale: Vector3f?
        var rotation: Vector3f?
        var leftRotation: Quaternionf?
        var rightRotation: Quaternionf?
        var billboardMode: BillboardMode?
        var conditionalPlaceholder: String?

        fun build(): T
    }
}