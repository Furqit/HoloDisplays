package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object MainMenu {

    fun openMainMenu(player: ServerPlayer) {
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_3x3,
            player = player,
            title = "HoloDisplays",
            size = 9,
            borderSlots = listOf(1, 3, 5, 7)
        )

        gui.apply {
            setSlot(1, GuiUtils.createGuiItem(
                name = "Create Hologram",
                item = Items.BOOK,
                lore = GuiUtils.createActionLore("Click to create a new hologram")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Hologram Name",
                    defaultText = "hologram",
                    onSubmit = { name ->
                        HologramManager.createHologram(name, player)
                        HologramEdit.open(player, name)
                    },
                    onCancel = { openMainMenu(player) }
                )
            }

            setSlot(3, GuiUtils.createGuiItem(
                name = "Manage Holograms",
                item = Items.BOOKSHELF,
                lore = GuiUtils.createActionLore("Click to view and manage holograms")
            )) { _, _, _, _ -> HologramList.open(player) }

            setSlot(5, GuiUtils.createGuiItem(
                name = "Manage Displays",
                item = Items.ITEM_FRAME,
                lore = GuiUtils.createActionLore("Click to view and manage displays")
            )) { _, _, _, _ -> DisplayList.open(player) }

            setSlot(7, GuiUtils.createGuiItem(
                name = "Create Display",
                item = Items.ARMOR_STAND,
                lore = GuiUtils.createActionLore("Click to create a new display")
            )) { _, _, _, _ -> CreateDisplay.open(player) }

            open()
        }
    }
}