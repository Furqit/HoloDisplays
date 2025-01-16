package dev.furq.holodisplays.data.display

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

data class ItemDisplay(
    val id: String,
    val itemDisplayType: String = "ground",
    override val rotation: Vector3f? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<ItemDisplay> {
        var id: String = ""
        var itemDisplayType: String = "ground"
        override var rotation: Vector3f? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = ItemDisplay(id, itemDisplayType, rotation, scale, billboardMode)
    }
}