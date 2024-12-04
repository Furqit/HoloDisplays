package dev.furq.holodisplays.menu

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.ItemDisplay
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object ItemEditMenu : LineEditMenu() {
    fun show(source: ServerCommandSource, name: String) {
        val display = DisplayConfig.getDisplay(name)!!
        val itemDisplay = display.display as ItemDisplay

        addEmptyLines(source)
        showHeader(source)

        source.sendFeedback({
            Text.literal("✦ ")
                .formatted(Formatting.GREEN)
                .append(
                    Text.literal("Item Display Editor")
                        .formatted(Formatting.WHITE)
                )
                .append(
                    Text.literal(" » ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(name)
                        .formatted(Formatting.GREEN)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        showSectionHeader(source, "Item Properties")
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Item: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(itemDisplay.id)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name item id ", Formatting.GREEN))
        }, false)
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Display Type: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(itemDisplay.itemDisplayType)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name item displayType ", Formatting.GREEN))
        }, false)

        showSectionFooter(source)
        source.sendFeedback({ Text.literal("") }, false)
        showSectionHeader(source, "Common Properties")
        showCommonProperties(source, name, itemDisplay)
        showSectionFooter(source)

        showFooter(source, "/holo list display")
    }
}