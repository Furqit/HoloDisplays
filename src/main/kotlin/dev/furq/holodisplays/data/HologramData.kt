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

    data class DisplayLine(
        val text: String? = null,
        val item: String? = null,
        val block: String? = null,
    ) {
        init {
            require(text != null || item != null || block != null) {
                "DisplayLine must have at least one of: text, item, or block"
            }
            require(listOf(text, item, block).count { it != null } == 1) {
                "DisplayLine must have exactly one of: text, item, or block"
            }
        }

        fun getReference(): String = text ?: item ?: block ?: throw IllegalStateException("Invalid display line state")
    }
}