package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object HologramList {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayerEntity, page: Int = 0) {
        val holograms = HologramConfig.getHolograms().toList()
        val maxPages = (holograms.size - 1) / ITEMS_PER_PAGE
        val currentPage = page.coerceIn(0, maxPages)

        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
        gui.title = Text.literal("Holograms (${currentPage + 1}/${maxPages + 1})")

        for (i in 0..44) {
            if (i !in 10..16 && i !in 19..25 && i !in 28..34) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
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
                open(player, currentPage - 1)
            }
        }

        gui.setSlot(40, GuiItems.createBackItem()) { _, _, _, _ ->
            MainMenu.openMainMenu(player)
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
                open(player, currentPage + 1)
            }
        }

        var slot = 10
        val startIndex = currentPage * ITEMS_PER_PAGE
        val endIndex = minOf(startIndex + ITEMS_PER_PAGE, holograms.size)

        for (i in startIndex until endIndex) {
            val (name, hologram) = holograms[i]
            if (slot in listOf(17, 26, 35)) {
                slot += 2
            }

            gui.setSlot(slot, GuiElement(
                GuiItems.createGuiItem(
                    item = Items.BOOK,
                    name = name,
                    lore = listOf(
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Left-Click to edit").formatted(Formatting.GRAY)),
                        Text.empty()
                            .append(Text.literal("→").formatted(Formatting.YELLOW))
                            .append(Text.literal(" Right-Click to delete").formatted(Formatting.GRAY))
                    )
                )
            ) { _, type, _, _ ->
                if (type.isRight) {
                    DeleteConfirmation.open(player, name, "hologram") {
                        open(player, currentPage)
                    }
                } else {
                    HologramEdit.open(player, name)
                }
            })
            slot++
        }

        gui.open()
    }
}