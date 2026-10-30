package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object TextDisplayEditor {
    private val alignmentModes = listOf("left", "center", "right")

    fun open(
        player: ServerPlayer,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = Lang.get(player, "gui.text_display_editor.title", "Edit Component Display"),
            size = 45,
            borderSlots = listOf(10, 12, 14, 16, 29, 31, 33, 36)
        )

        gui.apply {
            setSlot(10, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.lines", "Component Lines"),
                item = Items.PAPER,
                lore = buildList {
                    addAll(GuiUtils.createLore(Lang.get(player, "gui.text_display_editor.current_lines", "Current Lines:")))
                    display.lines.forEachIndexed { index, line ->
                        add(Component.literal("${index + 1}. $line").withStyle(ChatFormatting.WHITE))
                    }
                    addAll(GuiUtils.createActionLore(
                        Lang.get(player, "gui.text_display_editor.left_click_add_line", "Left-Click to add line"),
                        Lang.get(player, "gui.text_display_editor.right_click_edit_lines", "Right-Click to edit lines")
                    ))
                }
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> addNewLine(player, name, returnCallback)
                    type.isRightClick() -> TextLineEditor.open(player, name)
                }
            }

            setSlot(12, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.line_width", "Line Width"),
                item = Items.STRING,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to display.lineWidth.toString(),
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ ->
                AnvilInput.open(player, Lang.get(player, "gui.text_display_editor.enter_line_width", "Enter Line Width (1-200)"), display.lineWidth.toString(),
                    onSubmit = { width ->
                        DisplayManager.updateLineWidth(name, width.toInt(), player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(14, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.background_color", "Background Color"),
                item = Items.PAINTING,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to display.backgroundColor.toString(),
                    Lang.get(player, "gui.text_display_editor.left_click_change", "Left Click to change"),
                    Lang.get(player, "gui.text_display_editor.right_click_reset", "Right Click to reset")
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editBackgroundColor(player, name, returnCallback)
                    type.isRightClick() -> {
                        DisplayManager.updateBackground(name, null, null, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    }
                }
            }

            setSlot(16, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.opacity", "Component Opacity"),
                item = Items.GLASS,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to display.textOpacity.toString(),
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ ->
                AnvilInput.open(player, Lang.get(player, "gui.text_display_editor.enter_opacity", "Enter Opacity (1-100)"), display.textOpacity.toString(),
                    onSubmit = { opacity ->
                        DisplayManager.updateTextOpacity(name, opacity.toInt(), player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(29, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.shadow", "Shadow"),
                //~ if >=26.2 'GRAY_DYE' -> 'DYE.gray'
                item = Items.DYE.gray,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to (display.shadow ?: false).toString(),
                    Lang.get(player, "gui.common.click_toggle", "Click to toggle")
                )
            )) { _, _, _, _ ->
                val currentValue = display.shadow ?: false
                DisplayManager.updateShadow(name, !currentValue, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            setSlot(31, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.see_through", "See Through"),
                item = Items.GLASS,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to (display.seeThrough
                        ?: false).toString(),
                    Lang.get(player, "gui.common.click_toggle", "Click to toggle")
                )
            )) { _, _, _, _ ->
                val currentValue = display.seeThrough ?: false
                DisplayManager.updateSeeThrough(name, !currentValue, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            setSlot(33, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.text_display_editor.alignment", "Component Alignment"),
                item = Items.LECTERN,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore(Lang.get(player, "gui.common.current", "Current"), Lang.enumHint(player, "alignment", display.alignment?.name?.uppercase() ?: "CENTER")))
                    addAll(GuiUtils.createActionLore(Lang.get(player, "gui.common.click_cycle", "Click to cycle through modes")))
                    addAll(GuiUtils.createLore("Available modes: LEFT, CENTER, RIGHT"))
                }
            )) { _, _, _, _ ->
                val currentMode = display.alignment?.name?.lowercase() ?: "center"
                val currentIndex = alignmentModes.indexOf(currentMode)
                val nextMode = alignmentModes[(currentIndex + 1) % alignmentModes.size]
                DisplayManager.updateAlignment(name, nextMode, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            GuiUtils.setupBackButton(this, 36) { returnCallback() }
            open()
        }
    }

    private fun addNewLine(player: ServerPlayer, name: String, returnCallback: () -> Unit) {
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return
        AnvilInput.open(
            player = player,
            title = Lang.get(player, "gui.common.enter_new_line", "Enter New Line"),
            defaultText = "",
            onSubmit = { text ->
                val lines = display.lines.toMutableList().apply { add(text) }
                DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
                open(player, name, returnCallback)
            },
            onCancel = { open(player, name, returnCallback) }
        )
    }

    private fun editBackgroundColor(player: ServerPlayer, name: String, returnCallback: () -> Unit) {
        AnvilInput.open(player, Lang.get(player, "gui.text_display_editor.enter_color", "Enter Color (hex)"), "FFFFFF",
            onSubmit = { color ->
                AnvilInput.open(player, Lang.get(player, "gui.text_display_editor.enter_opacity_range", "Enter Opacity (0-100)"), "100",
                    onSubmit = { opacity ->
                        DisplayManager.updateBackground(name, color, opacity.toInt(), player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            },
            onCancel = { open(player, name, returnCallback) }
        )
    }
}