package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object BlockDisplayEditor {
    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.display as? BlockDisplay ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        gui.title = Text.literal("Edit Block Display")

        for (i in 0..8) {
            if (i !in listOf(4, 6)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            4, GuiItems.createGuiItem(
                name = "Block",
                item = Items.GRASS_BLOCK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.id).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            AnvilInput.open(
                player = player,
                title = "Enter Block ID",
                defaultText = display.id
            ) { blockId ->
                Utils.updateDisplayBlock(name, blockId, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(6, GuiItems.createBackItem()) { _, _, _, _ ->
            DisplayEdit.open(player, name, returnCallback)
        }

        gui.open()
    }
}