package dev.furq.holodisplays.data

import dev.furq.holodisplays.data.display.BaseDisplay
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay

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
    }
}