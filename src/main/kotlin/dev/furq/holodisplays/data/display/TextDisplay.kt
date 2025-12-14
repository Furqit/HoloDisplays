@file:UseSerializers(Vector3fSerializer::class, QuaternionfSerializer::class)

package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.utils.QuaternionfSerializer
import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Quaternionf
import org.joml.Vector3f

@Serializable
data class TextDisplay(
    val lines: List<String>,
    val lineWidth: Int? = null,
    val backgroundColor: String? = null,
    val textOpacity: Int? = null,
    val shadow: Boolean? = null,
    val seeThrough: Boolean? = null,
    val alignment: TextAlignment? = null,
    override val rotation: Vector3f? = null,
    override val leftRotation: Quaternionf? = null,
    override val rightRotation: Quaternionf? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {

    @Serializable
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
        override var leftRotation: Quaternionf? = null
        override var rightRotation: Quaternionf? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = TextDisplay(
            lines.toList(), lineWidth, backgroundColor, textOpacity,
            shadow, seeThrough, alignment, rotation, leftRotation, rightRotation, scale, billboardMode, conditionalPlaceholder
        )
    }
}