@file:UseSerializers(Vector3fSerializer::class)

package dev.furq.holodisplays.data

import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

@Serializable
data class HologramData(
    val displays: List<DisplayLine>,
    val position: Position,
    val rotation: Vector3f = Vector3f(),
    val scale: Vector3f = Vector3f(1.0f),
    val billboardMode: BillboardMode = BillboardMode.CENTER,
    val updateRate: Int = 20,
    val viewRange: Double = 48.0,
    val conditionalPlaceholder: String? = null,
) {
    @Serializable
    data class DisplayLine(
        val name: String,
        val offset: Vector3f = Vector3f(),
    )

    @Serializable
    data class Position(
        val world: String = "minecraft:overworld",
        val x: Float,
        val y: Float,
        val z: Float,
    ) {
        fun toVec3f() = Vector3f(x, y, z)
    }

    val world: String get() = position.world

    class Builder {
        var displays = mutableListOf<DisplayLine>()
        var position: Position = Position(world = "minecraft:overworld", x = 0.0f, y = 0.0f, z = 0.0f)
        var rotation: Vector3f = Vector3f()
        var scale: Vector3f = Vector3f(1.0f)
        var billboardMode: BillboardMode = BillboardMode.CENTER
        var updateRate: Int = 20
        var viewRange: Double = 48.0
        var conditionalPlaceholder: String? = null

        fun build() = HologramData(
            displays.toList(),
            position,
            rotation,
            scale,
            billboardMode,
            updateRate,
            viewRange,
            conditionalPlaceholder
        )
    }
}