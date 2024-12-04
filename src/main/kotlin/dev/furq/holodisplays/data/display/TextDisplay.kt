package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode

data class TextDisplay(
    val lines: MutableList<String>,
    val lineWidth: Int? = null,
    val backgroundColor: String? = null,
    val textOpacity: Int? = null,
    val shadow: Boolean? = null,
    val seeThrough: Boolean? = null,
    val alignment: TextAlignment? = null,
    override val rotation: Rotation? = null,
    override val scale: Scale? = null,
    override val billboardMode: BillboardMode? = null,
) : BaseDisplay() {
    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }

    class Builder : BaseDisplay.Builder<TextDisplay> {
        var lines = mutableListOf<String>()
        var lineWidth: Int? = null
        var backgroundColor: String? = null
        var textOpacity: Int? = null
        var shadow: Boolean? = null
        var seeThrough: Boolean? = null
        var alignment: TextAlignment? = null
        override var rotation: Rotation? = null
        override var scale: Scale? = null
        override var billboardMode: BillboardMode? = null

        override fun build() = TextDisplay(
            lines, lineWidth, backgroundColor, textOpacity,
            shadow, seeThrough, alignment, rotation, scale, billboardMode
        )
    }
}