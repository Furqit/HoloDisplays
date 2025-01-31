package dev.furq.holodisplays.gui

import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object MainMenu {
    fun openMainMenu(player: ServerPlayerEntity) {
        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        gui.title = Text.literal("HoloDisplays")

        for (i in 0..8) {
            if (i !in listOf(1, 3, 5, 7)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            1, GuiItems.createGuiItem(
                name = "Create Hologram",
                item = Items.BOOK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Click to create a new hologram").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Hologram Name",
                defaultText = "hologram",
                onSubmit = { name ->
                    Utils.createHologram(name, player)
                    HologramEdit.open(player, name)
                },
                onCancel = { openMainMenu(player) }
            )
        }

        gui.setSlot(
            3, GuiItems.createGuiItem(
                name = "Manage Holograms",
                item = Items.BOOKSHELF,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Click to view and manage holograms").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            HologramList.open(player)
        }

        gui.setSlot(
            5, GuiItems.createGuiItem(
                name = "Manage Displays",
                item = Items.ITEM_FRAME,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Click to view and manage displays").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            DisplayList.open(player)
        }

        gui.setSlot(
            7, GuiItems.createGuiItem(
                name = "Create Display",
                item = Items.ARMOR_STAND,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→ ").formatted(Formatting.YELLOW))
                        .append(Text.literal("Click to view and manage holograms").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            CreateDisplay.open(player)
        }

        gui.open()
    }
}