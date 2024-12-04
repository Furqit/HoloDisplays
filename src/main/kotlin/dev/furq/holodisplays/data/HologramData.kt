package dev.furq.holodisplays.data

import dev.furq.holodisplays.data.common.Offset
import dev.furq.holodisplays.data.common.Position
import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class HologramData(
    val displays: MutableList<DisplayLine>,
    var position: Position,
    var scale: Scale = Scale(),
    var billboardMode: BillboardMode = BillboardMode.CENTER,
    var updateRate: Int = 20,
    var viewRange: Double = 48.0,
    var rotation: Rotation = Rotation(),
) {
    data class DisplayLine(
        val displayId: String,
        val offset: Offset = Offset(),
    )

    class Builder {
        var displays = mutableListOf<DisplayLine>()
        var position: Position = Position()
        var scale: Scale = Scale()
        var billboardMode: BillboardMode = BillboardMode.CENTER
        var updateRate: Int = 20
        var viewRange: Double = 16.0
        var rotation: Rotation = Rotation()

        fun build() = HologramData(displays, position, scale, billboardMode, updateRate, viewRange, rotation)
    }
}