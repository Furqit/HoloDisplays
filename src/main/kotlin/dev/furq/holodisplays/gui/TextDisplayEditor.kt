package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TextDisplayEditor {
    private val alignmentModes = listOf("left", "center", "right")

    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = "Edit Text Display",
            size = 45,
            borderSlots = listOf(10, 12, 14, 16, 29, 31, 33, 36)
        )

        gui.apply {
            setSlot(10, GuiUtils.createGuiItem(
                name = "Text Lines",
                item = Items.PAPER,
                lore = buildList {
                    addAll(GuiUtils.createLore("Current Lines:"))
                    display.lines.forEachIndexed { index, line ->
                        add(Text.literal("${index + 1}. $line").formatted(Formatting.WHITE))
                    }
                    addAll(GuiUtils.createActionLore("Left-Click to add line", "Right-Click to edit lines"))
                }
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> addNewLine(player, name, returnCallback)
                    type.isRight -> TextLineEditor.open(player, name)
                }
            }

            setSlot(12, GuiUtils.createGuiItem(
                name = "Line Width",
                item = Items.STRING,
                lore = GuiUtils.createCombinedLore("Current" to display.lineWidth.toString(), "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(player, "Enter Line Width (1-200)", display.lineWidth.toString(),
                    onSubmit = { width ->
                        DisplayManager.updateLineWidth(name, width.toInt(), player.commandSource)
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(14, GuiUtils.createGuiItem(
                name = "Background Color",
                item = Items.PAINTING,
                lore = GuiUtils.createCombinedLore(
                    "Current" to display.backgroundColor.toString(),
                    "Left Click to change", "Right Click to reset"
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editBackgroundColor(player, name, returnCallback)
                    type.isRight -> {
                        DisplayManager.resetBackground(name, player.commandSource)
                        open(player, name, returnCallback)
                    }
                }
            }

            setSlot(16, GuiUtils.createGuiItem(
                name = "Text Opacity",
                item = Items.GLASS,
                lore = GuiUtils.createCombinedLore("Current" to display.textOpacity.toString(), "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(player, "Enter Opacity (1-100)", display.textOpacity.toString(),
                    onSubmit = { opacity ->
                        DisplayManager.updateTextOpacity(name, opacity.toInt(), player.commandSource)
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(29, GuiUtils.createGuiItem(
                name = "Shadow",
                item = Items.GRAY_DYE,
                lore = GuiUtils.createCombinedLore("Current" to (display.shadow ?: false).toString(), "Click to toggle")
            )) { _, _, _, _ ->
                val currentValue = display.shadow ?: false
                DisplayManager.updateShadow(name, !currentValue, player.commandSource)
                open(player, name, returnCallback)
            }

            setSlot(31, GuiUtils.createGuiItem(
                name = "See Through",
                item = Items.GLASS,
                lore = GuiUtils.createCombinedLore("Current" to (display.seeThrough
                    ?: false).toString(), "Click to toggle")
            )) { _, _, _, _ ->
                val currentValue = display.seeThrough ?: false
                DisplayManager.updateSeeThrough(name, !currentValue, player.commandSource)
                open(player, name, returnCallback)
            }

            setSlot(33, GuiUtils.createGuiItem(
                name = "Text Alignment",
                item = Items.LECTERN,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Current", display.alignment?.name?.uppercase() ?: "CENTER"))
                    addAll(GuiUtils.createActionLore("Click to cycle through modes"))
                    addAll(GuiUtils.createLore("Available modes: LEFT, CENTER, RIGHT"))
                }
            )) { _, _, _, _ ->
                val currentMode = display.alignment?.name?.lowercase() ?: "center"
                val currentIndex = alignmentModes.indexOf(currentMode)
                val nextMode = alignmentModes[(currentIndex + 1) % alignmentModes.size]
                DisplayManager.updateAlignment(name, nextMode, player.commandSource)
                open(player, name, returnCallback)
            }

            GuiUtils.setupBackButton(this, 36) { returnCallback() }
            open()
        }
    }

    private fun addNewLine(player: ServerPlayerEntity, name: String, returnCallback: () -> Unit) {
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return
        AnvilInput.open(
            player = player,
            title = "Enter New Line",
            defaultText = "",
            onSubmit = { text ->
                val lines = display.lines.toMutableList().apply { add(text) }
                DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
                open(player, name, returnCallback)
            },
            onCancel = { open(player, name, returnCallback) }
        )
    }

    private fun editBackgroundColor(player: ServerPlayerEntity, name: String, returnCallback: () -> Unit) {
        AnvilInput.open(player, "Enter Color (hex)", "FFFFFF",
            onSubmit = { color ->
                AnvilInput.open(player, "Enter Opacity (1-100)", "100",
                    onSubmit = { opacity ->
                        DisplayManager.updateBackground(name, color, opacity.toInt(), player.commandSource)
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            },
            onCancel = { open(player, name, returnCallback) }
        )
    }
}