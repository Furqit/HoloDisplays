package dev.furq.holodisplays.data

import dev.furq.holodisplays.config.DisplayConfig
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class HologramData(
    val displays: MutableList<DisplayLine>,
    var position: Position,
    var scale: Float = 1f,
    var billboardMode: BillboardMode = BillboardMode.CENTER,
    var updateRate: Int = 20,
    var viewRange: Double = 16.0,
    var rotation: Rotation = Rotation(),
) {
    data class Position(val world: String = "minecraft:world", val x: Float, val y: Float, val z: Float)
    data class Rotation(val pitch: Float = 0f, val yaw: Float = 0f)
    data class Offset(val x: Float = 0.0f, val y: Float = -0.3f, val z: Float = 0.0f)

    data class DisplayLine(
        val displayId: String,
        val offset: Offset = Offset()
    )
}