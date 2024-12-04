package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class ItemDisplay(
    val id: String,
    val itemDisplayType: String = "ground",
    override val rotation: Rotation? = null,
    override val scale: Scale? = null,
    override val billboardMode: BillboardMode? = null,
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<ItemDisplay> {
        var id: String = ""
        var itemDisplayType: String = "ground"
        override var rotation: Rotation? = null
        override var scale: Scale? = null
        override var billboardMode: BillboardMode? = null

        override fun build() = ItemDisplay(id, itemDisplayType, rotation, scale, billboardMode)
    }
}