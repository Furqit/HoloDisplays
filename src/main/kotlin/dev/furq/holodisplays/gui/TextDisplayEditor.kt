package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object TextDisplayEditor {
    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Edit Text Display")

        for (i in 0..44) {
            if (i !in listOf(10, 12, 14, 16, 29, 31, 33, 36)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            10, GuiItems.createGuiItem(
                name = "Text",
                item = Items.PAPER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.lines.firstOrNull() ?: "").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Text",
                defaultText = display.lines.firstOrNull() ?: "",
                onSubmit = { text ->
                    Utils.updateDisplayText(name, text, player.commandSource)
                    open(player, name)
                },
                onCancel = { open(player, name) }
            )
        }

        gui.setSlot(
            12, GuiItems.createGuiItem(
                name = "Line Width",
                item = Items.STRING,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.lineWidth.toString()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Line Width (1-200)", display.lineWidth.toString(),
                onSubmit = { width ->
                    Utils.updateDisplayLineWidth(name, width.toInt(), player.commandSource)
                    open(player, name)
                },
                onCancel = { open(player, name) }
            )
        }

        gui.setSlot(
            14, GuiItems.createGuiItem(
                name = "Background Color",
                item = Items.PAINTING,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.backgroundColor.toString()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Left Click to change").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right Click to reset").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isLeft) {
                AnvilInput.open(player, "Enter Color (hex)", "FFFFFF",
                    onSubmit = { color ->
                        AnvilInput.open(player, "Enter Opacity (1-100)", "100",
                            onSubmit = { opacity ->
                                Utils.updateDisplayBackground(name, color, opacity.toInt(), player.commandSource)
                                open(player, name)
                            },
                            onCancel = { open(player, name) }
                        )
                    },
                    onCancel = { open(player, name) }
                )
            } else if (type.isRight) {
                Utils.resetDisplayBackground(name, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            16, GuiItems.createGuiItem(
                name = "Text Opacity",
                item = Items.GLASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.textOpacity.toString()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Opacity (1-100)", display.textOpacity.toString(),
                onSubmit = { opacity ->
                    Utils.updateDisplayTextOpacity(name, opacity.toInt(), player.commandSource)
                    open(player, name)
                },
                onCancel = { open(player, name) }
            )
        }

        gui.setSlot(
            29, GuiItems.createGuiItem(
                name = "Shadow",
                item = Items.GRAY_DYE,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal((display.shadow ?: false).toString()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to toggle").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            val currentValue = display.shadow ?: false
            Utils.updateDisplayShadow(name, !currentValue, player.commandSource)
            open(player, name)
        }

        gui.setSlot(
            31, GuiItems.createGuiItem(
                name = "See Through",
                item = Items.GLASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal((display.seeThrough ?: false).toString()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to toggle").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            val currentValue = display.seeThrough ?: false
            Utils.updateDisplaySeeThrough(name, !currentValue, player.commandSource)
            open(player, name)
        }

        gui.setSlot(
            33, GuiItems.createGuiItem(
                name = "Text Alignment",
                item = Items.LECTERN,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal(display.alignment?.name?.uppercase() ?: "CENTER").formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to cycle through modes").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("Available modes: ").formatted(Formatting.GRAY))
                        .append(Text.literal("LEFT, CENTER, RIGHT").formatted(Formatting.WHITE))
                )
            )
        ) { _, _, _, _ ->
            val modes = listOf("left", "center", "right")
            val currentMode = display.alignment?.name?.lowercase() ?: "center"
            val currentIndex = modes.indexOf(currentMode)
            val nextMode = modes[(currentIndex + 1) % modes.size]
            Utils.updateDisplayAlignment(name, nextMode, player.commandSource)
            open(player, name)
        }

        gui.setSlot(36, GuiItems.createBackItem()) { _, _, _, _ ->
            DisplayEdit.open(player, name, returnCallback)
        }

        gui.open()
    }
}