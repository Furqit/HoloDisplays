package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.Lang
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
                    lore = GuiUtils.createActionLore(Lang.get(player, "gui.common.click_confirm", "Click to confirm"))
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
                name = Lang.get(player, "gui.common.cancel", "Cancel"),
                item = Items.BARRIER,
                lore = GuiUtils.createActionLore(Lang.get(player, "gui.common.click_cancel", "Click to cancel"))
            )) { _, _, _, _ ->
                close()
                onCancel()
            }

            setSlot(0, GuiUtils.createGuiItem(
                name = defaultText,
                item = Items.PAPER,
                lore = GuiUtils.createActionLore(Lang.get(player, "gui.anvil_input.hint", "Type in the text field above"))
            )) { _, _, _, _ -> }

            open()
        }
    }
}