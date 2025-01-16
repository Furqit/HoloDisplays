package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector3f

object HologramDisplays {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayerEntity, hologramName: String, page: Int = 0) {
        val hologram = HologramConfig.getHologram(hologramName) ?: return
        val maxPages = (hologram.displays.size - 1) / ITEMS_PER_PAGE
        val currentPage = page.coerceIn(0, maxPages)

        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Manage Lines (${currentPage + 1}/${maxPages + 1})")

        for (i in 0..44) {
            if (i !in 10..16 && i !in 19..25 && i !in 28..34) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        var slot = 10
        val startIndex = currentPage * ITEMS_PER_PAGE
        val endIndex = minOf(startIndex + ITEMS_PER_PAGE, hologram.displays.size)

        for (i in startIndex until endIndex) {
            val display = hologram.displays[i]
            if (slot in listOf(17, 26, 35)) {
                slot += 2
            }

            gui.setSlot(
                slot, GuiItems.createGuiItem(
                    name = "Line ${i + 1}",
                    item = Items.PAPER,
                    lore = listOf(
                        Text.empty()
                            .append(Text.literal("Display: ").formatted(Formatting.GRAY))
                            .append(Text.literal(display.displayId).formatted(Formatting.WHITE)),
                        Text.empty()
                            .append(Text.literal("Offset: ").formatted(Formatting.GRAY))
                            .append(
                                Text.literal("${display.offset.x}, ${display.offset.y}, ${display.offset.z}")
                                    .formatted(Formatting.WHITE)
                            ),
                        Text.empty(),
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Left-Click to edit offset").formatted(Formatting.GRAY)),
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Right-Click to remove").formatted(Formatting.GRAY))
                    )
                )
            ) { _, type, _, _ ->
                if (type.isRight) {
                    HologramHandler.updateHologramProperty(
                        hologramName,
                        HologramHandler.HologramProperty.RemoveLine(i)
                    )
                    open(player, hologramName, currentPage)
                } else {
                    AnvilInput.open(player, "Enter X Offset", display.offset.x.toString()) { x ->
                        AnvilInput.open(player, "Enter Y Offset", display.offset.y.toString()) { y ->
                            AnvilInput.open(player, "Enter Z Offset", display.offset.z.toString()) { z ->
                                HologramHandler.updateHologramProperty(
                                    hologramName,
                                    HologramHandler.HologramProperty.LineOffset(
                                        i,
                                        Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
                                    )
                                )
                                open(player, hologramName, currentPage)
                            }
                        }
                    }
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
                open(player, hologramName, currentPage - 1)
            }
        }

        gui.setSlot(40, GuiItems.createBackItem()) { _, _, _, _ ->
            HologramEdit.open(player, hologramName)
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
                open(player, hologramName, currentPage + 1)
            }
        }

        gui.setSlot(
            43, GuiItems.createGuiItem(
                name = "Add Line",
                item = Items.EMERALD,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Left-Click to create new display").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right-Click to use existing display").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isRight) {
                DisplayList.open(
                    player = player,
                    selectionMode = true,
                    hologramName = hologramName,
                    onSelect = { displayName ->
                        HologramHandler.updateHologramProperty(
                            hologramName,
                            HologramHandler.HologramProperty.AddLine(displayName)
                        )
                        open(player, hologramName, currentPage)
                    }
                )
            } else {
                CreateDisplay.open(player, hologramName)
            }
        }

        gui.open()
    }
}