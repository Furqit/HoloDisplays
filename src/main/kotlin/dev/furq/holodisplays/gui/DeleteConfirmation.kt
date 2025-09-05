package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object DeleteConfirmation {
    private val hologramManager = HologramManager()
    private val displayManager = DisplayManager()

    fun open(player: ServerPlayerEntity, name: String, type: String, returnGui: () -> Unit) {
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X3,
            player = player,
            title = "Confirm Deletion",
            size = 27,
            borderSlots = listOf(11, 15)
        )

        gui.apply {
            setSlot(11, GuiUtils.createGuiItem(
                name = "Confirm",
                item = Items.LIME_CONCRETE,
                lore = buildList {
                    addAll(GuiUtils.createActionLore("Click to delete"))
                    addAll(GuiUtils.createCurrentValueLore("Type", type))
                    addAll(GuiUtils.createCurrentValueLore("Name", name))
                }
            )) { _, _, _, _ ->
                when (type) {
                    "hologram" -> hologramManager.deleteHologram(name, player.commandSource)
                    "display" -> displayManager.deleteDisplay(name, player.commandSource)
                }
                returnGui()
            }

            setSlot(15, GuiUtils.createGuiItem(
                name = "Cancel",
                item = Items.RED_CONCRETE,
                lore = GuiUtils.createActionLore("Click to cancel")
            )) { _, _, _, _ -> returnGui() }

            open()
        }
    }
}