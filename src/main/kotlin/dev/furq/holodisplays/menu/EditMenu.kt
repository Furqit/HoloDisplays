package dev.furq.holodisplays.menu

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting

data object EditMenu : LineEditMenu() {
    fun showHologram(source: ServerCommandSource, name: String) {
        val hologram = HologramConfig.getHologram(name) ?: run {
            source.sendError(Text.literal("⚠ Hologram not found").formatted(Formatting.RED))
            return
        }

        addEmptyLines(source)
        showHeader(source)

        source.sendFeedback({
            Text.literal("✦ ")
                .formatted(Formatting.GREEN)
                .append(
                    Text.literal("Hologram Editor")
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
        showSectionHeader(source, "Properties")

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("Position: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal("${hologram.position.x}, ${hologram.position.y}, ${hologram.position.z}")
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", "/holo move $name ", Formatting.GREEN))
                .append(Text.literal(" "))
                .append(createRunButton("Here", "/holo move $name", Formatting.YELLOW))
        }, false)

        showProperty(
            source,
            "Scale",
            "${hologram.scale.x}, ${hologram.scale.y}, ${hologram.scale.z}",
            "/holo edit hologram $name scale "
        )
        showProperty(source, "Billboard", hologram.billboardMode.toString(), "/holo edit hologram $name billboard ")
        showProperty(source, "Update Rate", "${hologram.updateRate} ticks", "/holo edit hologram $name updateRate ")
        showProperty(source, "View Range", "${hologram.viewRange} blocks", "/holo edit hologram $name viewRange ")
        showProperty(
            source,
            "Rotation",
            "${hologram.rotation.pitch}, ${hologram.rotation.yaw}, ${hologram.rotation.roll}",
            "/holo edit hologram $name rotation "
        )

        showSectionFooter(source)
        source.sendFeedback({ Text.literal("") }, false)
        showSectionHeader(source, "Displays")

        hologram.displays.forEachIndexed { index, line ->
            val display = DisplayConfig.getDisplay(line.displayId)

            val (icon, displayName) = when (display?.displayType) {
                is DisplayData.DisplayType.Text -> "✎" to "Text: ${line.displayId}"
                is DisplayData.DisplayType.Item -> "✦" to "Item: ${line.displayId}"
                is DisplayData.DisplayType.Block -> "■" to "Block: ${line.displayId}"
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
                        Text.literal(displayName)
                            .formatted(Formatting.WHITE)
                    )
                    .append(Text.literal(" "))
                    .append(createRunButton("Edit", "/holo edit display ${line.displayId}", Formatting.GREEN))
                    .append(Text.literal(" "))
                    .append(createRunButton("Remove", "/holo line $name remove $index", Formatting.RED))
            }, false)

            source.sendFeedback({
                Text.literal("│   ")
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
                        Text.literal("${line.offset.x}, ${line.offset.y}, ${line.offset.z}")
                            .formatted(Formatting.WHITE)
                    )
                    .append(Text.literal(" "))
                    .append(createButton("Edit", "/holo line $name offset $index ", Formatting.GREEN))
            }, false)
        }

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("Add Existing: ")
                        .formatted(Formatting.GRAY)
                )
                .append(createButton("Add", "/holo line $name add ", Formatting.GREEN))
                .append(Text.literal(" "))
        }, false)

        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("Create New: ")
                        .formatted(Formatting.GRAY)
                )
                .append(createButton("Text", "/holo create display $name text ", Formatting.YELLOW))
                .append(Text.literal(" "))
                .append(createButton("Item", "/holo create display $name item ", Formatting.YELLOW))
                .append(Text.literal(" "))
                .append(createButton("Block", "/holo create display $name block ", Formatting.YELLOW))
        }, false)

        showSectionFooter(source)
        showFooter(source, "/holo list hologram")
    }

    private fun showProperty(
        source: ServerCommandSource,
        label: String,
        value: String,
        editCommand: String,
        resetLabel: String? = null,
    ) {
        source.sendFeedback({
            Text.literal("│ ")
                .formatted(Formatting.GRAY)
                .append(
                    Text.literal("• ")
                        .formatted(Formatting.GREEN)
                )
                .append(
                    Text.literal("$label: ")
                        .formatted(Formatting.GRAY)
                )
                .append(
                    Text.literal(value)
                        .formatted(Formatting.WHITE)
                )
                .append(Text.literal(" "))
                .append(createButton("Edit", editCommand, Formatting.GREEN))
                .also { text ->
                    if (resetLabel != null) {
                        text.append(Text.literal(" "))
                            .append(createRunButton(resetLabel, "${editCommand}default", Formatting.YELLOW))
                    }
                }
        }, false)
    }

    fun showDisplay(source: ServerCommandSource, name: String) {
        val display = DisplayConfig.getDisplay(name) ?: run {
            source.sendError(Text.literal("⚠ Display not found").formatted(Formatting.RED))
            return
        }

        when (display.displayType) {
            is DisplayData.DisplayType.Text -> TextEditMenu.show(source, name)
            is DisplayData.DisplayType.Item -> ItemEditMenu.show(source, name)
            is DisplayData.DisplayType.Block -> BlockEditMenu.show(source, name)
        }
    }
}