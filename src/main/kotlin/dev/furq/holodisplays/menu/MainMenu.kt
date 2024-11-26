package dev.furq.holodisplays.menu

import dev.furq.holodisplays.HoloDisplays
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object MainMenu : LineEditMenu() {
    fun show(source: ServerCommandSource) {
        addEmptyLines(source)
        showHeader(source)

        source.sendFeedback({
            Text.literal("✧ ")
                .formatted(Formatting.GREEN)
                .append(
                    Text.literal("HoloDisplays")
                        .formatted(Formatting.WHITE, Formatting.BOLD)
                )
                .append(
                    Text.literal(" v${HoloDisplays.VERSION}")
                        .formatted(Formatting.GRAY)
                )
        }, false)

        source.sendFeedback({ Text.literal("") }, false)

        showSectionHeader(source, "Hologram Management")
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("✦ ")
                        .formatted(Formatting.GREEN)
                )
                .append(createRunButton("List Holograms", "/holo list hologram", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createButton("Create Hologram", "/holo create hologram ", Formatting.GREEN))
        }, false)
        showSectionFooter(source)

        source.sendFeedback({ Text.literal("") }, false)

        showSectionHeader(source, "Display Management")
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("✦ ")
                        .formatted(Formatting.GREEN)
                )
                .append(createRunButton("List Displays", "/holo list display", Formatting.GREEN))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("✧ ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Create: ")
                        .formatted(Formatting.GRAY)
                )
                .append(createButton("Text", "/holo create display text ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createButton("Item", "/holo create display item ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createButton("Block", "/holo create display block ", Formatting.GREEN))
        }, false)
        showSectionFooter(source)

        source.sendFeedback({ Text.literal("") }, false)

        showSectionHeader(source, "Help & Information")
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("✦ ")
                        .formatted(Formatting.YELLOW)
                )
                .append(
                    Text.literal("「GitHub」")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.YELLOW)
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://github.com/Furq07/HoloDisplays"
                                    )
                                )
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("View source on GitHub")
                                    )
                                )
                        )
                )
                .append(Text.literal(" "))
                .append(
                    Text.literal("「Discord」")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.YELLOW)
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://discord.gg/XhZzmvzPDV"
                                    )
                                )
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Join our Discord server")
                                    )
                                )
                        )
                )
                .append(Text.literal(" "))
                .append(
                    Text.literal("「Wiki」")
                        .setStyle(
                            Style.EMPTY
                                .withColor(Formatting.YELLOW)
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://github.com/Furq07/HoloDisplays/wiki"
                                    )
                                )
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("Open Wiki in browser")
                                    )
                                )
                        )
                )
        }, false)
        showSectionFooter(source)
        showFooter(source)
    }
}