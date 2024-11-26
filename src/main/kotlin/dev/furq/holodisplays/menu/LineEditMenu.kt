package dev.furq.holodisplays.menu

import dev.furq.holodisplays.data.DisplayData
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.abs

sealed class LineEditMenu {
    protected fun addEmptyLines(source: ServerCommandSource, count: Int = 3) {
        repeat(count) {
            source.sendFeedback({ Text.literal("") }, false)
        }
    }

    protected fun createButton(label: String, command: String, color: Formatting): Text {
        val hoverText = when {
            label.equals("Delete", ignoreCase = true) -> "⚠ Click to delete"
            else -> "Click to $label"
        }

        return Text.literal("「")
            .formatted(Formatting.GRAY)
            .append(
                Text.literal(label)
                    .setStyle(
                        Style.EMPTY
                            .withColor(color)
                            .withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(hoverText).formatted(
                                        if (label.equals("Delete", ignoreCase = true)) Formatting.RED else color
                                    )
                                )
                            )
                    )
            )
            .append(Text.literal("」").formatted(Formatting.GRAY))
    }

    protected fun createRunButton(label: String, command: String, color: Formatting): Text {
        val hoverText = when {
            label.equals("Delete", ignoreCase = true) -> "⚠ Click to delete"
            else -> "Click to $label"
        }

        return Text.literal("「")
            .formatted(Formatting.GRAY)
            .append(
                Text.literal(label)
                    .setStyle(
                        Style.EMPTY
                            .withColor(color)
                            .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.literal(hoverText).formatted(
                                        if (label.equals("Delete", ignoreCase = true)) Formatting.RED else color
                                    )
                                )
                            )
                    )
            )
            .append(Text.literal("」").formatted(Formatting.GRAY))
    }

    private fun createBackButton(source: ServerCommandSource, command: String, label: String = "Back to Menu") {
        source.sendFeedback({
            Text.literal("「")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal(label)
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.GRAY)
                                .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Return to previous menu")
                                    )
                                )
                        )
                )
                .append(Text.literal("」").formatted(Formatting.GRAY))
        }, false)
    }

    protected fun showCommonProperties(
        source: ServerCommandSource,
        name: String,
        displayType: DisplayData.DisplayType,
    ) {
        source.sendFeedback({
            Text.literal("  • Scale: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("${displayType.scale}").formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name scale ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Reset", "/holo edit display $name scale reset", Formatting.YELLOW))
        }, false)

        source.sendFeedback({
            Text.literal("  • Billboard: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("${displayType.billboardMode}").formatted(Formatting.WHITE))
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name billboard ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Reset", "/holo edit display $name billboard reset", Formatting.YELLOW))
        }, false)

        source.sendFeedback({
            Text.literal("  • Rotation: ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("${displayType.rotation?.pitch}, ${displayType.rotation?.yaw}")
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name rotation ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Reset", "/holo edit display $name rotation reset", Formatting.YELLOW))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Offset: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    if (abs(displayType.offset.x) > 16 || abs(displayType.offset.y) > 16 || abs(displayType.offset.z) > 16)
                        Text.literal("${displayType.offset.x}, ${displayType.offset.y}, ${displayType.offset.z}")
                            .formatted(Formatting.RED)
                    else
                        Text.literal("${displayType.offset.x}, ${displayType.offset.y}, ${displayType.offset.z}")
                            .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name offset ", Formatting.GREEN))
        }, false)
    }

    protected fun showHeader(source: ServerCommandSource) {
        source.sendFeedback({
            Text.literal("╔════════════════════════════╗")
                .formatted(Formatting.GREEN)
        }, false)
    }

    protected fun showFooter(source: ServerCommandSource, backCommand: String? = null) {
        source.sendFeedback({ Text.literal("") }, false)
        source.sendFeedback({
            Text.literal("╚════════════════════════════╝")
                .formatted(Formatting.GREEN)
        }, false)
        if (backCommand != null) {
            createBackButton(source, backCommand)
        }
    }

    protected fun showSectionHeader(source: ServerCommandSource, title: String) {
        source.sendFeedback({
            Text.literal("┌─ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal(title)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" ").formatted(Formatting.GRAY))
                .append(
                    Text.literal("─".repeat(24 - title.length))
                        .formatted(Formatting.GRAY)
                )
        }, false)
    }

    protected fun showSectionFooter(source: ServerCommandSource) {
        source.sendFeedback({
            Text.literal("└" + "─".repeat(28))
                .formatted(Formatting.GRAY)
        }, false)
    }
}