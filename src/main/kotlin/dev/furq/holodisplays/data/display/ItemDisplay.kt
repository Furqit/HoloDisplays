@file:UseSerializers(Vector3fSerializer::class)

package dev.furq.holodisplays.data.display

import dev.furq.holodisplays.utils.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import org.joml.Vector3f

@Serializable
data class ItemDisplay(
    val id: String,
    val itemDisplayType: String = "ground",
    val customModelData: Int? = null,
    override val rotation: Vector3f? = null,
    override val scale: Vector3f? = null,
    override val billboardMode: BillboardMode? = null,
    override val conditionalPlaceholder: String? = null
) : BaseDisplay() {
    class Builder : BaseDisplay.Builder<ItemDisplay> {
        var id: String = ""
        var itemDisplayType: String = "ground"
        var customModelData: Int? = null
        override var rotation: Vector3f? = null
        override var scale: Vector3f? = null
        override var billboardMode: BillboardMode? = null
        override var conditionalPlaceholder: String? = null

        override fun build() = ItemDisplay(id, itemDisplayType, customModelData, rotation, scale, billboardMode, conditionalPlaceholder)
    }
}