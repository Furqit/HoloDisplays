package dev.furq.holodisplays.gui

import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object CreateDisplay {
    fun open(player: ServerPlayerEntity, hologramName: String? = null) {
        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        gui.title = Text.literal("Create Display")

        for (i in 0..8) {
            if (i !in listOf(3, 4, 5, 6)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            3, GuiItems.createGuiItem(
                name = "Text Display",
                item = Items.PAPER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to create a new text display").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Display Name",
                defaultText = "text_display",
                onSubmit = { name ->
                    AnvilInput.open(
                        player = player,
                        title = "Enter Text",
                        defaultText = "Hello World!",
                        onSubmit = { text ->
                            if (Utils.createTextDisplay(name, text, player.commandSource)) {
                                if (hologramName != null) {
                                    Utils.addDisplayToHologram(hologramName, name, player.commandSource)
                                    DisplayEdit.open(player, name) {
                                        HologramDisplays.open(player, hologramName)
                                    }
                                } else {
                                    DisplayEdit.open(player, name)
                                }
                            }
                        },
                        onCancel = { open(player, hologramName) }
                    )
                },
                onCancel = { open(player, hologramName) }
            )
        }

        gui.setSlot(
            4, GuiItems.createGuiItem(
                name = "Item Display",
                item = Items.ITEM_FRAME,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to create a new item display").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Display Name",
                defaultText = "item_display",
                onSubmit = { name ->
                    AnvilInput.open(
                        player = player,
                        title = "Enter Item ID",
                        defaultText = "minecraft:diamond_sword",
                        onSubmit = { itemId ->
                            if (Utils.createItemDisplay(name, itemId, player.commandSource)) {
                                if (hologramName != null) {
                                    Utils.addDisplayToHologram(hologramName, name, player.commandSource)
                                    DisplayEdit.open(player, name) {
                                        HologramDisplays.open(player, hologramName)
                                    }
                                } else {
                                    DisplayEdit.open(player, name)
                                }
                            }
                        },
                        onCancel = { open(player, hologramName) }
                    )
                },
                onCancel = { open(player, hologramName) }
            )
        }

        gui.setSlot(
            5, GuiItems.createGuiItem(
                name = "Block Display",
                item = Items.GRASS_BLOCK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to create a new block display").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Display Name",
                defaultText = "block_display",
                onSubmit = { name ->
                    AnvilInput.open(
                        player = player,
                        title = "Enter Block ID",
                        defaultText = "minecraft:grass_block",
                        onSubmit = { blockId ->
                            if (Utils.createBlockDisplay(name, blockId, player.commandSource)) {
                                if (hologramName != null) {
                                    Utils.addDisplayToHologram(hologramName, name, player.commandSource)
                                    DisplayEdit.open(player, name) {
                                        HologramDisplays.open(player, hologramName)
                                    }
                                } else {
                                    DisplayEdit.open(player, name)
                                }
                            }
                        },
                        onCancel = { open(player, hologramName) }
                    )
                },
                onCancel = { open(player, hologramName) }
            )
        }

        gui.setSlot(6, GuiItems.createBackItem()) { _, _, _, _ ->
            if (hologramName != null) {
                HologramEdit.open(player, hologramName)
            } else {
                MainMenu.openMainMenu(player)
            }
        }

        gui.open()
    }
}