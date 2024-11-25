package dev.furq.holodisplays.menu

import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object DeleteConfirmMenu {
    fun showHologram(source: ServerCommandSource, name: String) {
        showConfirmation(
            source,
            "Delete Hologram",
            "hologram",
            name,
            "/holo edit hologram $name",
            "/holo delete hologram $name confirm"
        )
    }

    fun showDisplay(source: ServerCommandSource, name: String) {
        showConfirmation(
            source,
            "Delete Display",
            "display",
            name,
            "/holo edit display $name",
            "/holo delete display $name confirm"
        )
    }

    private fun showConfirmation(
        source: ServerCommandSource,
        title: String,
        type: String,
        name: String,
        cancelCommand: String,
        deleteCommand: String,
    ) {
        addEmptyLines(source)

        source.sendFeedback({
            Text.literal("╔══ ")
                .formatted(Formatting.RED)
                .append(
                    Text.literal("⚠ WARNING ")
                        .formatted(Formatting.RED, Formatting.BOLD)
                )
                .append(
                    Text.literal("══╗")
                        .formatted(Formatting.RED)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        source.sendFeedback({
            Text.literal("  ")
                .append(
                    Text.literal(title)
                        .formatted(Formatting.WHITE, Formatting.BOLD)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        source.sendFeedback({
            Text.literal("  Are you sure you want to delete")
                .formatted(Formatting.WHITE)
        }, false)

        source.sendFeedback({
            Text.literal("  the $type ")
                .formatted(Formatting.WHITE)
                .append(
                    Text.literal(name)
                        .formatted(Formatting.YELLOW, Formatting.BOLD)
                )
                .append(
                    Text.literal("?")
                        .formatted(Formatting.WHITE)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        source.sendFeedback({
            Text.literal("  ⚠ ")
                .formatted(Formatting.RED)
                .append(
                    Text.literal("This action cannot be undone!")
                        .formatted(Formatting.RED, Formatting.BOLD)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        source.sendFeedback({
            Text.literal("  ")
                .append(
                    Text.literal("「Cancel」")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.GREEN)
                                .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, cancelCommand))
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Return to $type edit menu")
                                            .formatted(Formatting.GREEN)
                                    )
                                )
                        )
                )
                .append(Text.literal("   "))
                .append(
                    Text.literal("「Delete」")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.RED)
                                .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, deleteCommand))
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("⚠ Permanently delete this $type")
                                            .formatted(Formatting.RED)
                                    )
                                )
                        )
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        source.sendFeedback({
            Text.literal("╚════════════════════════╝")
                .formatted(Formatting.RED)
        }, false)
    }

    private fun addEmptyLines(source: ServerCommandSource) {
        repeat(2) {
            source.sendFeedback({ Text.literal("") }, false)
        }
    }
}