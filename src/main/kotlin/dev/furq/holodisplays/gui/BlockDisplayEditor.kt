package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object BlockDisplayEditor {
    private val displayManager = DisplayManager()

    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.display as? BlockDisplay ?: return
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_3X3,
            player = player,
            title = "Edit Block Display",
            size = 9,
            borderSlots = listOf(4, 6)
        )

        gui.apply {
            setSlot(4, GuiUtils.createGuiItem(
                name = "Block",
                item = Items.GRASS_BLOCK,
                lore = GuiUtils.createCombinedLore("Current" to display.id, "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Block ID",
                    defaultText = display.id,
                    onSubmit = { blockId ->
                        displayManager.updateBlockId(name, blockId, player.commandSource)
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            GuiUtils.setupBackButton(this, 6) { returnCallback() }
            open()
        }
    }
}