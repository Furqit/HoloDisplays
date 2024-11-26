package dev.furq.holodisplays.data

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class DisplayData(
    val displayType: DisplayType,
) {
    data class Offset(
        val x: Float = 0.0f,
        val y: Float = -0.3f,
        val z: Float = 0.0f,
    )

    sealed class DisplayType {
        abstract val scale: Float?
        abstract val billboardMode: BillboardMode?
        abstract val rotation: HologramData.Rotation?
        abstract val offset: Offset

        data class Text(
            val lines: MutableList<String>,
            override val rotation: HologramData.Rotation? = null,
            val lineWidth: Int? = null,
            val backgroundColor: String? = null,
            val textOpacity: Int? = null,
            val shadow: Boolean? = null,
            val seeThrough: Boolean? = null,
            val alignment: TextAlignment? = null,
            override val scale: Float? = null,
            override val billboardMode: BillboardMode? = null,
            override val offset: Offset = Offset(),
        ) : DisplayType()

        data class Item(
            val id: String,
            val itemDisplayType: String = "ground",
            override val rotation: HologramData.Rotation? = null,
            override val scale: Float? = null,
            override val billboardMode: BillboardMode? = null,
            override val offset: Offset = Offset(),
        ) : DisplayType()

        data class Block(
            val id: String,
            override val rotation: HologramData.Rotation? = null,
            override val scale: Float? = null,
            override val billboardMode: BillboardMode? = null,
            override val offset: Offset = Offset(),
        ) : DisplayType()
    }

    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }
}