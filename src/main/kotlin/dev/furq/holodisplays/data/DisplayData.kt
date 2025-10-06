package dev.furq.holodisplays.data

import dev.furq.holodisplays.data.display.*
import kotlinx.serialization.Serializable

@Serializable
data class DisplayData(
    val type: BaseDisplay,
) {
    companion object {
        fun text(builder: TextDisplay.Builder.() -> Unit): DisplayData {
            val textBuilder = TextDisplay.Builder()
            textBuilder.builder()
            return DisplayData(textBuilder.build())
        }

        fun item(builder: ItemDisplay.Builder.() -> Unit): DisplayData {
            val itemBuilder = ItemDisplay.Builder()
            itemBuilder.builder()
            return DisplayData(itemBuilder.build())
        }

        fun block(builder: BlockDisplay.Builder.() -> Unit): DisplayData {
            val blockBuilder = BlockDisplay.Builder()
            blockBuilder.builder()
            return DisplayData(blockBuilder.build())
        }

        fun entity(builder: EntityDisplay.Builder.() -> Unit): DisplayData {
            val entityBuilder = EntityDisplay.Builder()
            entityBuilder.builder()
            return DisplayData(entityBuilder.build())
        }
    }
}