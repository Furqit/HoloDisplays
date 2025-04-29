package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object DeleteConfirmation {
    private val hologramManager = HologramManager()
    private val displayManager = DisplayManager()

    fun open(player: ServerPlayerEntity, name: String, type: String, returnGui: () -> Unit) {
        val gui = SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false)
        gui.title = Text.literal("Confirm Deletion")

        for (i in 0..26) {
            if (i !in listOf(11, 15)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            11, GuiItems.createGuiItem(
                name = "Confirm",
                item = Items.LIME_CONCRETE,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to delete").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("Type: ").formatted(Formatting.GRAY))
                        .append(Text.literal(type).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("Name: ").formatted(Formatting.GRAY))
                        .append(Text.literal(name).formatted(Formatting.WHITE))
                )
            )
        ) { _, _, _, _ ->
            when (type) {
                "hologram" -> hologramManager.deleteHologram(name, player.commandSource)
                "display" -> displayManager.deleteDisplay(name, player.commandSource)
            }
            returnGui()
        }

        gui.setSlot(
            15, GuiItems.createGuiItem(
                name = "Cancel",
                item = Items.RED_CONCRETE,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to cancel").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            returnGui()
        }

        gui.open()
    }
}