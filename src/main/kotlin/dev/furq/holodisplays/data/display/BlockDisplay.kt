package dev.furq.holodisplays.data.display

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

data class BlockDisplay(
    val id: String,
    override val rotation: Vector3f? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<BlockDisplay> {
        var id: String = ""
        override var rotation: Vector3f? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = BlockDisplay(id, rotation, scale, billboardMode)
    }
}