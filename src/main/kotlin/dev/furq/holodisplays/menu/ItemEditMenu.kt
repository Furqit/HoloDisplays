package dev.furq.holodisplays.menu

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.DisplayData
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object ItemEditMenu : LineEditMenu() {
    fun show(source: ServerCommandSource, name: String) {
        val display = DisplayConfig.getDisplay(name)?.displayType as? DisplayData.DisplayType.Item ?: run {
            source.sendError(Text.literal("⚠ Display not found").formatted(Formatting.RED))
            return
        }

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
                    Text.literal(display.id)
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
                    Text.literal(display.itemDisplayType)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name item displayType ", Formatting.GREEN))
        }, false)

        showSectionFooter(source)
        source.sendFeedback({ Text.literal("") }, false)
        showSectionHeader(source, "Common Properties")
        showCommonProperties(source, name, display)
        showSectionFooter(source)

        showFooter(source, "/holo list display")
    }
}