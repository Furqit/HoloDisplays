package dev.furq.holodisplays.gui

import dev.furq.holodisplays.utils.GuiUtils
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items

object AnvilInput {
    fun open(
        player: ServerPlayer,
        title: String,
        defaultText: String = "",
        onSubmit: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        object : AnvilInputGui(player, false) {
            override fun onInput(text: String) {
                setSlot(2, GuiUtils.createGuiItem(
                    name = text,
                    item = Items.PAPER,
                    lore = GuiUtils.createActionLore("Click to confirm")
                )) { _, _, _, _ ->
                    text.takeIf { it.isNotEmpty() }?.let {
                        onSubmit(it)
                        close()
                    }
                }
            }
        }.apply {
            this.title = Component.literal(title)
            setSlot(1, GuiUtils.createGuiItem(
                name = "Cancel",
                item = Items.BARRIER,
                lore = GuiUtils.createActionLore("Click to cancel")
            )) { _, _, _, _ ->
                close()
                onCancel()
            }

            setSlot(0, GuiUtils.createGuiItem(
                name = defaultText,
                item = Items.PAPER,
                lore = GuiUtils.createActionLore("Type in the text field above")
            )) { _, _, _, _ -> }

            open()
        }
    }
}