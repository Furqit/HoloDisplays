package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object TextLineEditor {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayerEntity, displayName: String, page: Int = 0) {
        val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return
        val pageInfo = GuiUtils.calculatePageInfo(display.lines.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = GuiUtils.createPagedTitle("Edit Text Lines", pageInfo),
            size = 45,
            borderSlots = (10..16) + (19..25) + (28..34)
        )

        gui.apply {
            GuiUtils.setupPaginationButtons(
                gui = this,
                pageInfo = pageInfo,
                onPrevious = { open(player, displayName, pageInfo.currentPage - 1) },
                onNext = { open(player, displayName, pageInfo.currentPage + 1) }
            )

            GuiUtils.setupBackButton(this, 40) { TextDisplayEditor.open(player, displayName) }

            var slot = 10
            val startIndex = pageInfo.currentPage * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, display.lines.size)

            for (i in startIndex until endIndex) {
                if (slot in listOf(17, 26, 35)) slot += 2

                val line = display.lines[i]
                val lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Content", line))
                    addAll(GuiUtils.createActionLore("Left-Click to edit", "Right-Click to delete"))
                }

                setSlot(slot, GuiUtils.createGuiItem(
                    name = "Line ${i + 1}",
                    item = Items.PAPER,
                    lore = lore
                )) { _, type, _, _ ->
                    when {
                        type.isLeft -> editLine(player, displayName, i, line, pageInfo.currentPage)
                        type.isRight -> deleteLine(player, displayName, i, pageInfo.currentPage)
                    }
                }
                slot++
            }

            setSlot(43, GuiUtils.createGuiItem(
                name = "Add Line",
                item = Items.EMERALD,
                lore = GuiUtils.createActionLore("Click to add new line")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter New Line",
                    defaultText = "",
                    onSubmit = { text ->
                        val lines = display.lines.toMutableList().apply { add(text) }
                        DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                        FeedbackManager.send(player.commandSource, FeedbackType.TEXT_UPDATED, "text" to text)
                        open(player, displayName, pageInfo.currentPage)
                    },
                    onCancel = { open(player, displayName, pageInfo.currentPage) }
                )
            }

            open()
        }
    }

    private fun editLine(player: ServerPlayerEntity, displayName: String, lineIndex: Int, currentText: String, currentPage: Int) {
        AnvilInput.open(
            player = player,
            title = "Edit Line ${lineIndex + 1}",
            defaultText = currentText,
            onSubmit = { newText ->
                val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return@open
                val lines = display.lines.toMutableList().apply { this[lineIndex] = newText }
                DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                FeedbackManager.send(player.commandSource, FeedbackType.TEXT_UPDATED, "text" to newText)
                open(player, displayName, currentPage)
            },
            onCancel = { open(player, displayName, currentPage) }
        )
    }

    private fun deleteLine(player: ServerPlayerEntity, displayName: String, lineIndex: Int, currentPage: Int) {
        val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return
        val lines = display.lines.toMutableList().apply { removeAt(lineIndex) }
        DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(player.commandSource, FeedbackType.TEXT_UPDATED, "text" to "line removed")
        open(player, displayName, currentPage)
    }
}