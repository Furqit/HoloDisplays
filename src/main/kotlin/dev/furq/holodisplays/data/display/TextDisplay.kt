package dev.furq.holodisplays.data.display

import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

data class TextDisplay(
    val lines: List<String>,
    val lineWidth: Int? = null,
    val backgroundColor: String? = null,
    val textOpacity: Int? = null,
    val shadow: Boolean? = null,
    val seeThrough: Boolean? = null,
    val alignment: TextAlignment? = null,
    override val rotation: Vector3f? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {

    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }

    private val cachedText by lazy { lines.joinToString("\n") }
    fun getText(): String = cachedText

    class Builder : BaseDisplay.Builder<TextDisplay> {
        var lines = mutableListOf<String>()
        var lineWidth: Int? = null
        var backgroundColor: String? = null
        var textOpacity: Int? = null
        var shadow: Boolean? = null
        var seeThrough: Boolean? = null
        var alignment: TextAlignment? = null
        override var rotation: Vector3f? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = TextDisplay(
            lines.toList(), lineWidth, backgroundColor, textOpacity,
            shadow, seeThrough, alignment, rotation, scale, billboardMode, conditionalPlaceholder
        )
    }
}