package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TextLineEditor {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayerEntity, displayName: String, page: Int = 0) {
        val display = DisplayConfig.getDisplay(displayName)?.display as? TextDisplay ?: return
        val maxPages = (display.lines.size - 1) / ITEMS_PER_PAGE
        val currentPage = page.coerceIn(0, maxPages)

        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Edit Text Lines (${currentPage + 1}/${maxPages + 1})")

        for (i in 0..44) {
            if (i !in 10..16 && i !in 19..25 && i !in 28..34) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        var slot = 10
        val startIndex = currentPage * ITEMS_PER_PAGE
        val endIndex = minOf(startIndex + ITEMS_PER_PAGE, display.lines.size)

        for (i in startIndex until endIndex) {
            val line = display.lines[i]
            if (slot in listOf(17, 26, 35)) {
                slot += 2
            }

            gui.setSlot(
                slot, GuiItems.createGuiItem(
                    name = "Line ${i + 1}",
                    item = Items.PAPER,
                    lore = listOf(
                        Text.empty()
                            .append(Text.literal("Content: ").formatted(Formatting.GRAY))
                            .append(Text.literal(line).formatted(Formatting.WHITE)),
                        Text.empty(),
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Left-Click to edit").formatted(Formatting.GRAY)),
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Right-Click to delete").formatted(Formatting.GRAY))
                    )
                )
            ) { _, type, _, _ ->
                if (type.isLeft) {
                    AnvilInput.open(
                        player = player,
                        title = "Edit Line ${i + 1}",
                        defaultText = line,
                        onSubmit = { newText ->
                            val lines = display.lines.toMutableList()
                            lines[i] = newText
                            DisplayHandler.updateDisplayProperty(
                                displayName,
                                DisplayHandler.DisplayProperty.TextLines(lines)
                            )
                            open(player, displayName, currentPage)
                        },
                        onCancel = { open(player, displayName, currentPage) }
                    )
                } else if (type.isRight) {
                    val lines = display.lines.toMutableList()
                    lines.removeAt(i)
                    DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                    open(player, displayName, currentPage)
                }
            }
            slot++
        }

        if (currentPage > 0) {
            gui.setSlot(
                39, GuiItems.createGuiItem(
                    item = Items.ARROW,
                    name = "Previous Page",
                    lore = listOf(
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Click to go to page $currentPage").formatted(Formatting.GRAY))
                    )
                )
            ) { _, _, _, _ ->
                open(player, displayName, currentPage - 1)
            }
        }

        gui.setSlot(40, GuiItems.createBackItem()) { _, _, _, _ ->
            TextDisplayEditor.open(player, displayName)
        }

        if (currentPage < maxPages) {
            gui.setSlot(
                41, GuiItems.createGuiItem(
                    item = Items.ARROW,
                    name = "Next Page",
                    lore = listOf(
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Click to go to page ${currentPage + 2}").formatted(Formatting.GRAY))
                    )
                )
            ) { _, _, _, _ ->
                open(player, displayName, currentPage + 1)
            }
        }

        gui.setSlot(
            43, GuiItems.createGuiItem(
                name = "Add Line",
                item = Items.EMERALD,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to add new line").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter New Line",
                defaultText = "",
                onSubmit = { text ->
                    val lines = display.lines.toMutableList()
                    lines.add(text)
                    DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                    open(player, displayName, currentPage)
                },
                onCancel = { open(player, displayName, currentPage) }
            )
        }

        gui.open()
    }
} 