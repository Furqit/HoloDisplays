package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object HologramEdit {
    fun open(player: ServerPlayerEntity, name: String) {
        val hologram = HologramConfig.getHologram(name) ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Edit Hologram")

        for (i in 0..44) {
            if (i !in listOf(10, 12, 14, 16, 28, 31, 34, 36)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            10, GuiItems.createGuiItem(
                name = "Position",
                item = Items.ENDER_PEARL,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal("${hologram.position.x}, ${hologram.position.y}, ${hologram.position.z}")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Left-Click to enter manually").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right-Click to set to current position").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isLeft) {
                AnvilInput.open(player, "Enter X Position", hologram.position.x.toString()) { x ->
                    AnvilInput.open(player, "Enter Y Position", hologram.position.y.toString()) { y ->
                        AnvilInput.open(player, "Enter Z Position", hologram.position.z.toString()) { z ->
                            Utils.updateHologramPosition(
                                name,
                                Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                player.world.registryKey.value.toString(),
                                player.commandSource
                            )
                            open(player, name)
                        }
                    }
                }
            } else if (type.isRight) {
                Utils.updateHologramPosition(
                    name,
                    Vector3f(player.x.toFloat(), player.y.toFloat(), player.z.toFloat()),
                    player.world.registryKey.value.toString(),
                    player.commandSource
                )
                open(player, name)
            }
        }

        gui.setSlot(
            12, GuiItems.createGuiItem(
                name = "View Range",
                item = Items.SPYGLASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal("${hologram.viewRange} blocks").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter View Range", hologram.viewRange.toString()) { range ->
                Utils.updateHologramViewRange(name, range.toFloat(), player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            14, GuiItems.createGuiItem(
                name = "Scale",
                item = Items.SCAFFOLDING,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal("${hologram.scale.x}, ${hologram.scale.y}, ${hologram.scale.z}")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter X Scale", hologram.scale.x.toString()) { x ->
                AnvilInput.open(player, "Enter Y Scale", hologram.scale.y.toString()) { y ->
                    AnvilInput.open(player, "Enter Z Scale", hologram.scale.z.toString()) { z ->
                        Utils.updateHologramScale(
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
            16, GuiItems.createGuiItem(
                name = "Rotation",
                item = Items.COMPASS,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal("${hologram.rotation.x}°, ${hologram.rotation.y}°, ${hologram.rotation.z}°")
                                .formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Pitch", hologram.rotation.x.toString()) { pitch ->
                AnvilInput.open(player, "Enter Yaw", hologram.rotation.y.toString()) { yaw ->
                    AnvilInput.open(player, "Enter Roll", hologram.rotation.z.toString()) { roll ->
                        Utils.updateHologramRotation(
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
            28, GuiItems.createGuiItem(
                name = "Billboard",
                item = Items.ITEM_FRAME,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal("${hologram.billboardMode}").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Billboard Mode", hologram.billboardMode.toString()) { billboard ->
                Utils.updateHologramBillboard(name, billboard, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            31, GuiItems.createGuiItem(
                name = "Update Rate",
                item = Items.CLOCK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal("${hologram.updateRate} ticks").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(player, "Enter Update Rate (ticks)", hologram.updateRate.toString()) { ticks ->
                Utils.updateHologramUpdateRate(name, ticks.toInt(), player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            34, GuiItems.createGuiItem(
                name = "Manage Displays",
                item = Items.BOOK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current Displays: ").formatted(Formatting.GRAY))
                        .append(Text.literal("${hologram.displays.size}").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to manage").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            HologramDisplays.open(player, name)
        }

        gui.setSlot(36, GuiItems.createBackItem()) { _, _, _, _ ->
            HologramList.open(player)
        }

        gui.open()
    }
}