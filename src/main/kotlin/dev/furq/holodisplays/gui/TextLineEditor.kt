package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.FeedbackType
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object TextLineEditor {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayer, displayName: String, page: Int = 0) {
        val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return
        val pageInfo = GuiUtils.calculatePageInfo(display.lines.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = GuiUtils.createPagedTitle(Lang.get(player, "gui.text_line_editor.title", "Edit Component Lines"), pageInfo),
            size = 45,
            borderSlots = GuiUtils.PAGED_BORDER_SLOTS
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
                slot = GuiUtils.advanceContentSlot(slot)

                val line = display.lines[i]
                val lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore(Lang.get(player, "gui.text_line_editor.content", "Content"), line))
                    addAll(GuiUtils.createActionLore(
                        Lang.get(player, "gui.common.left_click_edit", "Left-Click to edit"),
                        Lang.get(player, "gui.common.right_click_delete", "Right-Click to delete")
                    ))
                }

                setSlot(slot, GuiUtils.createGuiItem(
                    name = Lang.get(player, "gui.text_line_editor.line", "Line {number}", "number" to (i + 1)),
                    item = Items.PAPER,
                    lore = lore
                )) { _, type, _, _ ->
                    when {
                        type.isLeft -> editLine(player, displayName, i, line, pageInfo.currentPage)
                        type.isRightClick() -> deleteLine(player, displayName, i, pageInfo.currentPage)
                    }
                }
                slot++
            }

            setSlot(43, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_line_editor.add_line", "Add Line"),
                item = Items.EMERALD,
                lore = GuiUtils.createActionLore(Lang.get(player, "gui.text_line_editor.click_add", "Click to add new line"))
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = Lang.get(player, "gui.common.enter_new_line", "Enter New Line"),
                    defaultText = "",
                    onSubmit = { text ->
                        val lines = display.lines.toMutableList().apply { add(text) }
                        DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                        FeedbackManager.send(player.createCommandSourceStack(), FeedbackType.TEXT_UPDATED, "text" to text)
                        open(player, displayName, pageInfo.currentPage)
                    },
                    onCancel = { open(player, displayName, pageInfo.currentPage) }
                )
            }

            open()
        }
    }

    private fun editLine(player: ServerPlayer, displayName: String, lineIndex: Int, currentText: String, currentPage: Int) {
        AnvilInput.open(
            player = player,
            title = Lang.get(player, "gui.text_line_editor.edit_line", "Edit Line {number}", "number" to (lineIndex + 1)),
            defaultText = currentText,
            onSubmit = { newText ->
                val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return@open
                val lines = display.lines.toMutableList().apply { this[lineIndex] = newText }
                DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
                FeedbackManager.send(player.createCommandSourceStack(), FeedbackType.TEXT_UPDATED, "text" to newText)
                open(player, displayName, currentPage)
            },
            onCancel = { open(player, displayName, currentPage) }
        )
    }

    private fun deleteLine(player: ServerPlayer, displayName: String, lineIndex: Int, currentPage: Int) {
        val display = DisplayConfig.getDisplay(displayName)?.type as? TextDisplay ?: return
        val lines = display.lines.toMutableList().apply { removeAt(lineIndex) }
        DisplayHandler.updateDisplayProperty(displayName, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(player.createCommandSourceStack(), FeedbackType.LINE_REMOVED)
        open(player, displayName, currentPage)
    }
}