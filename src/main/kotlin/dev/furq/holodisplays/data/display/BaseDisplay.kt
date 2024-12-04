package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

abstract class BaseDisplay {
    abstract val scale: Scale?
    abstract val rotation: Rotation?
    abstract val billboardMode: BillboardMode?

    interface Builder<T : BaseDisplay> {
        var scale: Scale?
        var rotation: Rotation?
        var billboardMode: BillboardMode?

        fun build(): T
    }
}