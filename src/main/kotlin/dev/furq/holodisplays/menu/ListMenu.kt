package dev.furq.holodisplays.menu

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.DisplayData
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object ListMenu : LineEditMenu() {
    fun show(
        source: ServerCommandSource,
        title: String,
        items: List<String>,
        page: Int,
        entryRenderer: (String) -> Unit,
        paginationCommand: String,
        backCommand: String = "/holo",
    ) {
        addEmptyLines(source)
        showHeader(source)

        source.sendFeedback({
            Text.literal("✧ ")
                .formatted(Formatting.GREEN)
                .append(
                    Text.literal(title)
                        .formatted(Formatting.WHITE)
                )
                .append(
                    if (items.isNotEmpty()) {
                        val totalPages = (items.size + 9) / 10
                        val currentPage = page.coerceIn(1, totalPages)
                        Text.literal(" (Page $currentPage/$totalPages)")
                            .formatted(Formatting.GRAY)
                    } else Text.empty()
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        if (items.isEmpty()) {
            source.sendFeedback({
                Text.literal("│ ")
                    .formatted(Formatting.GRAY)
                    .append(
                        Text.literal("No $title found")
                            .formatted(Formatting.GRAY)
                    )
            }, false)
        } else {
            val totalPages = (items.size + 9) / 10
            val currentPage = page.coerceIn(1, totalPages)

            showSectionHeader(source, "Items")
            items.drop((currentPage - 1) * 10)
                .take(10)
                .forEach { entryRenderer(it) }
            showSectionFooter(source)

            if (totalPages > 1) {
                source.sendFeedback({ Text.literal("") }, false)
                source.sendFeedback({ createPaginationControls(currentPage, totalPages, paginationCommand) }, false)
            }
        }

        showFooter(source, backCommand)
    }

    private fun createPaginationControls(currentPage: Int, totalPages: Int, command: String): Text {
        return Text.empty()
            .append(
                if (currentPage > 1)
                    createRunButton("«", "$command${currentPage - 1}", Formatting.GREEN)
                else
                    Text.literal("«").formatted(Formatting.GRAY)
            )
            .append(Text.literal(" Page $currentPage/$totalPages ").formatted(Formatting.GRAY))
            .append(
                if (currentPage < totalPages)
                    createRunButton("»", "$command${currentPage + 1}", Formatting.GREEN)
                else
                    Text.literal("»").formatted(Formatting.GRAY)
            )
    }

    fun showHologramEntry(source: ServerCommandSource, name: String) {
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("✦ ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal(name)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createRunButton("Edit", "/holo edit hologram $name", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Delete", "/holo delete hologram $name", Formatting.RED))
        }, false)
    }

    fun showDisplayEntry(source: ServerCommandSource, name: String) {
        val display = DisplayConfig.getDisplay(name)
        val (icon, displayType) = when (display?.displayType) {
            is DisplayData.DisplayType.Text -> "✎" to "Text"
            is DisplayData.DisplayType.Item -> "✦" to "Item"
            is DisplayData.DisplayType.Block -> "■" to "Block"
            null -> "?" to "Unknown"
        }

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("$icon ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("$displayType: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(name)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createRunButton("Edit", "/holo edit display $name", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Delete", "/holo delete display $name", Formatting.RED))
        }, false)
    }
}