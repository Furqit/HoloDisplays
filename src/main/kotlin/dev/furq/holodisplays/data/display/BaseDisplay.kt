package dev.furq.holodisplays.data.display

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

abstract class BaseDisplay {
    abstract val scale: Vector3f?
    abstract val rotation: Vector3f?
    abstract val billboardMode: BillboardMode?

    interface Builder<T : BaseDisplay> {
        var scale: Vector3f?
        var rotation: Vector3f?
        var billboardMode: BillboardMode?
        var conditionalPlaceholder: String?

        fun build(): T
    }
}