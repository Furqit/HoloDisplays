package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class BlockDisplay(
    val id: String,
    override val rotation: Rotation? = null,
    override val scale: Scale? = null,
    override val billboardMode: BillboardMode? = null,
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<BlockDisplay> {
        var id: String = ""
        override var rotation: Rotation? = null
        override var scale: Scale? = null
        override var billboardMode: BillboardMode? = null

        override fun build() = BlockDisplay(id, rotation, scale, billboardMode)
    }
}