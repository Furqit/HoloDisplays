package dev.furq.holodisplays.data

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class DisplayData(
    val displayType: DisplayType,
) {
    sealed class DisplayType {
        abstract val scale: HologramData.Scale?
        abstract val billboardMode: BillboardMode?
        abstract val rotation: HologramData.Rotation?

        data class Text(
            val lines: MutableList<String>,
            val lineWidth: Int? = null,
            val backgroundColor: String? = null,
            val textOpacity: Int? = null,
            val shadow: Boolean? = null,
            val seeThrough: Boolean? = null,
            val alignment: TextAlignment? = null,
            override val rotation: HologramData.Rotation? = null,
            override val scale: HologramData.Scale? = null,
            override val billboardMode: BillboardMode? = null,
        ) : DisplayType()

        data class Item(
            val id: String,
            val itemDisplayType: String = "ground",
            override val rotation: HologramData.Rotation? = null,
            override val scale: HologramData.Scale? = null,
            override val billboardMode: BillboardMode? = null,
        ) : DisplayType()

        data class Block(
            val id: String,
            override val rotation: HologramData.Rotation? = null,
            override val scale: HologramData.Scale? = null,
            override val billboardMode: BillboardMode? = null,
        ) : DisplayType()
    }

    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }
}