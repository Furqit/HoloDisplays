package dev.furq.holodisplays.menu

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object TextEditMenu : LineEditMenu() {
    fun show(source: ServerCommandSource, name: String) {
        val display = DisplayConfig.getDisplay(name)!!
        val textDisplay = display.display as TextDisplay

        addEmptyLines(source)
        showHeader(source)

        source.sendFeedback({
            Text.literal("✎ ")
                .formatted(Formatting.GREEN)
                .append(
                    Text.literal("Text Display Editor")
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

        showSectionHeader(source, "Content")
        if (textDisplay.lines.isEmpty()) {
            source.sendFeedback({
                Text.literal("│ ")
                    .formatted(Formatting.GRAY)
                    .append(
                        Text.literal("No text lines added")
                            .formatted(Formatting.GRAY)
                    )
            }, false)
        } else {
            textDisplay.lines.forEachIndexed { index, line ->
                source.sendFeedback({
                    Text.literal("│ ")
                        .formatted(Formatting.GRAY)
                        .append(
                            Text.literal("${index + 1}. ")
                                .formatted(Formatting.GREEN)
                        )
                        .append(
                            Text.literal(line)
                                .formatted(Formatting.WHITE)
                        )
                        .append(Text.literal(" "))
                        .append(createButton("Edit", "/holo edit display $name text line $index ", Formatting.GREEN))
                        .append(Text.literal(" "))
                        .append(
                            createRunButton(
                                "Delete",
                                "/holo edit display $name text line delete $index",
                                Formatting.RED
                            )
                        )
                }, false)
            }
        }

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(createButton("Add Line", "/holo edit display $name text line add ", Formatting.GREEN))
        }, false)
        showSectionFooter(source)

        source.sendFeedback({ Text.literal("") }, false)
        showSectionHeader(source, "Text Properties")
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Alignment: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.alignment.toString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name text alignment ", Formatting.GREEN))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Line Width: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.lineWidth.toString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name text lineWidth ", Formatting.GREEN))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Background Color: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.backgroundColor ?: "None")
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name text backgroundColor ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(
                    createRunButton(
                        "Default",
                        "/holo edit display $name text backgroundColor default",
                        Formatting.YELLOW
                    )
                )
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Text Opacity: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.textOpacity.toString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo edit display $name text textOpacity ", Formatting.GREEN))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Shadow: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.shadow.toString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(
                    createRunButton(
                        "Toggle",
                        "/holo edit display $name text shadow ${!(textDisplay.shadow ?: false)}",
                        Formatting.GREEN
                    )
                )
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("See Through: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(textDisplay.seeThrough.toString())
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(
                    createRunButton(
                        "Toggle",
                        "/holo edit display $name text seeThrough ${!(textDisplay.seeThrough ?: false)}",
                        Formatting.GREEN
                    )
                )
        }, false)
        showSectionFooter(source)

        source.sendFeedback({ Text.literal("") }, false)

        showSectionHeader(source, "Common Properties")
        showCommonProperties(source, name, textDisplay)
        showSectionFooter(source)

        showFooter(source, "/holo list display")
    }
}