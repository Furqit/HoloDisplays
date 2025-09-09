package dev.furq.holodisplays.data

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

data class HologramData(
    val displays: List<DisplayLine>,
    val position: Vector3f,
    val world: String = "minecraft:overworld",
    val scale: Vector3f = Vector3f(1.0f),
    val billboardMode: BillboardMode = BillboardMode.CENTER,
    val updateRate: Int = 20,
    val viewRange: Double = 48.0,
    val rotation: Vector3f = Vector3f(),
    val conditionalPlaceholder: String? = null,
) {
    data class DisplayLine(
        val displayId: String,
        val offset: Vector3f = Vector3f(),
    )

    class Builder {
        var displays = mutableListOf<DisplayLine>()
        var position: Vector3f = Vector3f()
        var world: String = "minecraft:overworld"
        var scale: Vector3f = Vector3f(1.0f)
        var billboardMode: BillboardMode = BillboardMode.CENTER
        var updateRate: Int = 20
        var viewRange: Double = 48.0
        var rotation: Vector3f = Vector3f()
        var conditionalPlaceholder: String? = null

        fun build() = HologramData(
            displays.toList(),
            position,
            world,
            scale,
            billboardMode,
            updateRate,
            viewRange,
            rotation,
            conditionalPlaceholder
        )
    }
}