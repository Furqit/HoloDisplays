package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object DisplayEdit {
    private val displayManager = DisplayManager()

    fun open(player: ServerPlayerEntity, name: String, returnCallback: () -> Unit = { DisplayList.open(player) }) {
        val display = DisplayConfig.getDisplay(name) ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Edit Display")

        for (i in 0..44) {
            if (i !in listOf(11, 13, 15, 30, 32, 36)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            11, GuiItems.createGuiItem(
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
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right click to reset").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isRight) {
                displayManager.resetScale(name, player.commandSource)
                open(player, name, returnCallback)
            } else {
                AnvilInput.open(player, "Enter X Scale", display.display.scale?.x?.toString() ?: "1.0",
                    onSubmit = { x ->
                        AnvilInput.open(player, "Enter Y Scale", display.display.scale?.y?.toString() ?: "1.0",
                            onSubmit = { y ->
                                AnvilInput.open(player, "Enter Z Scale", display.display.scale?.z?.toString() ?: "1.0",
                                    onSubmit = { z ->
                                        displayManager.updateScale(
                                            name,
                                            Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                            player.commandSource
                                        )
                                        open(player, name, returnCallback)
                                    },
                                    onCancel = { open(player, name, returnCallback) }
                                )
                            },
                            onCancel = { open(player, name, returnCallback) }
                        )
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }
        }

        gui.setSlot(
            13, GuiItems.createGuiItem(
                name = "Billboard Mode",
                item = Items.COMPASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal(display.display.billboardMode?.name?.uppercase() ?: "NONE")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to cycle through modes").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right click to reset").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("Available modes: ").formatted(Formatting.GRAY))
                        .append(Text.literal("HORIZONTAL, VERTICAL, CENTER, FIXED").formatted(Formatting.WHITE))
                )
            )
        ) { _, type, _, _ ->
            if (type.isRight) {
                displayManager.resetBillboard(name, player.commandSource)
                open(player, name, returnCallback)
            } else {
                val modes = listOf("HORIZONTAL", "VERTICAL", "CENTER", "FIXED")
                val currentMode = display.display.billboardMode?.name ?: "FIXED"
                val currentIndex = modes.indexOf(currentMode)
                val nextMode = modes[(currentIndex + 1) % modes.size]
                displayManager.updateBillboard(name, nextMode.lowercase(), player.commandSource)
                open(player, name, returnCallback)
            }
        }

        gui.setSlot(
            15, GuiItems.createGuiItem(
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
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right click to reset").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isRight) {
                displayManager.resetRotation(name, player.commandSource)
                open(player, name, returnCallback)
            } else {
                AnvilInput.open(player, "Enter Pitch", display.display.rotation?.x?.toString() ?: "0",
                    onSubmit = { pitch ->
                        AnvilInput.open(player, "Enter Yaw", display.display.rotation?.y?.toString() ?: "0",
                            onSubmit = { yaw ->
                                AnvilInput.open(player, "Enter Roll", display.display.rotation?.z?.toString() ?: "0",
                                    onSubmit = { roll ->
                                        displayManager.updateRotation(
                                            name,
                                            pitch.toFloat(),
                                            yaw.toFloat(),
                                            roll.toFloat(),
                                            player.commandSource
                                        )
                                        open(player, name, returnCallback)
                                    },
                                    onCancel = { open(player, name, returnCallback) }
                                )
                            },
                            onCancel = { open(player, name, returnCallback) }
                        )
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }
        }

        gui.setSlot(
            30, GuiItems.createGuiItem(
                name = "Condition",
                item = Items.REPEATER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal(display.display.conditionalPlaceholder ?: "none").formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right-Click to remove").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isLeft) {
                AnvilInput.open(
                    player = player,
                    title = "Enter Value 1",
                    defaultText = "%player:name%",
                    onSubmit = { placeholder ->
                        AnvilInput.open(
                            player = player,
                            title = "Enter Operator",
                            defaultText = "=",
                            onSubmit = { operator ->
                                AnvilInput.open(
                                    player = player,
                                    title = "Enter Value 2",
                                    defaultText = "Furq_",
                                    onSubmit = { target ->
                                        val condition = "$placeholder $operator $target"
                                        displayManager.updateCondition(name, condition, player.commandSource)
                                        open(player, name, returnCallback)
                                    },
                                    onCancel = { open(player, name, returnCallback) }
                                )
                            },
                            onCancel = { open(player, name, returnCallback) }
                        )
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            } else if (type.isRight) {
                displayManager.updateCondition(name, null, player.commandSource)
                open(player, name, returnCallback)
            }
        }

        gui.setSlot(
            32, GuiItems.createGuiItem(
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

        gui.setSlot(36, GuiItems.createBackItem()) { _, _, _, _ ->
            returnCallback()
        }

        gui.open()
    }
}