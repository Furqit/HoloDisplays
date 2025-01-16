package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object DisplayEdit {
    fun open(player: ServerPlayerEntity, name: String) {
        val display = DisplayConfig.getDisplay(name) ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Edit Display")

        for (i in 0..44) {
            if (i !in listOf(10, 12, 14, 16, 34, 40)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            10, GuiItems.createGuiItem(
                name = "Scale",
                item = Items.SCAFFOLDING,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal("${display.display.scale?.x ?: 1.0}, ${display.display.scale?.y ?: 1.0}, ${display.display.scale?.z ?: 1.0}")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter X Scale", display.display.scale?.x?.toString() ?: "1.0") { x ->
                AnvilInput.open(player, "Enter Y Scale", display.display.scale?.y?.toString() ?: "1.0") { y ->
                    AnvilInput.open(player, "Enter Z Scale", display.display.scale?.z?.toString() ?: "1.0") { z ->
                        Utils.updateDisplayScale(
                            name,
                            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                            player.commandSource
                        )
                        open(player, name)
                    }
                }
            }
        }

        gui.setSlot(
            12, GuiItems.createGuiItem(
                name = "Billboard Mode",
                item = Items.COMPASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal(display.display.billboardMode?.name?.lowercase() ?: "none")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player,
                "Enter Billboard Mode (none/fixed/vertical/horizontal/center)",
                display.display.billboardMode?.name?.lowercase() ?: "none"
            ) { mode ->
                Utils.updateDisplayBillboard(name, mode, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            14, GuiItems.createGuiItem(
                name = "Rotation",
                item = Items.CLOCK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal("${display.display.rotation?.x ?: 0}°, ${display.display.rotation?.y ?: 0}°, ${display.display.rotation?.z ?: 0}°")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Pitch", display.display.rotation?.x?.toString() ?: "0") { pitch ->
                AnvilInput.open(player, "Enter Yaw", display.display.rotation?.y?.toString() ?: "0") { yaw ->
                    AnvilInput.open(player, "Enter Roll", display.display.rotation?.z?.toString() ?: "0") { roll ->
                        Utils.updateDisplayRotation(
                            name,
                            pitch.toFloat(),
                            yaw.toFloat(),
                            roll.toFloat(),
                            player.commandSource
                        )
                        open(player, name)
                    }
                }
            }
        }

        gui.setSlot(
            16, GuiItems.createGuiItem(
                name = "Type Settings",
                item = when (display.display) {
                    is TextDisplay -> Items.PAPER
                    is ItemDisplay -> Items.ITEM_FRAME
                    is BlockDisplay -> Items.GRASS_BLOCK
                    else -> Items.BARRIER
                },
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Type: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.display.javaClass.simpleName).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to edit type-specific settings").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            when (display.display) {
                is TextDisplay -> TextDisplayEditor.open(player, name)
                is ItemDisplay -> ItemDisplayEditor.open(player, name)
                is BlockDisplay -> BlockDisplayEditor.open(player, name)
            }
        }

        gui.setSlot(
            34, GuiItems.createGuiItem(
                name = "Delete Display",
                item = Items.BARRIER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to delete").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("Display: ").formatted(Formatting.GRAY))
                        .append(Text.literal(name).formatted(Formatting.WHITE))
                )
            )
        ) { _, _, _, _ ->
            DeleteConfirmation.open(player, name, "display") {
                DisplayList.open(player)
            }
        }

        gui.setSlot(40, GuiItems.createBackItem()) { _, _, _, _ ->
            DisplayList.open(player)
        }

        gui.open()
    }
}