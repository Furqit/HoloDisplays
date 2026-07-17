package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object DeleteConfirmation {

    fun open(player: ServerPlayer, name: String, type: String, returnGui: () -> Unit) {
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x3,
            player = player,
            title = "Confirm Deletion",
            size = 27,
            borderSlots = listOf(11, 15)
        )

        gui.apply {
            setSlot(11, GuiUtils.createGuiItem(
                name = "Confirm",
                //~ if >=26.2 'LIME_CONCRETE' -> 'CONCRETE.lime'
                item = Items.CONCRETE.lime,
                lore = buildList {
                    addAll(GuiUtils.createActionLore("Click to delete"))
                    addAll(GuiUtils.createCurrentValueLore("Type", type))
                    addAll(GuiUtils.createCurrentValueLore("Name", name))
                }
            )) { _, _, _, _ ->
                when (type) {
                    "hologram" -> HologramManager.deleteHologram(name, player.createCommandSourceStack())
                    "display" -> DisplayManager.deleteDisplay(name, player.createCommandSourceStack())
                }
                returnGui()
            }

            setSlot(15, GuiUtils.createGuiItem(
                name = "Cancel",
                //~ if >=26.2 'RED_CONCRETE' -> 'CONCRETE.red'
                item = Items.CONCRETE.red,
                lore = GuiUtils.createActionLore("Click to cancel")
            )) { _, _, _, _ -> returnGui() }

            open()
        }
    }
}