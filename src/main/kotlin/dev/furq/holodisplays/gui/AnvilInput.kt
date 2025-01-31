package dev.furq.holodisplays.gui

import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object AnvilInput {
    fun open(
        player: ServerPlayerEntity,
        title: String,
        defaultText: String = "",
        onSubmit: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val gui = object : AnvilInputGui(player, false) {
            override fun onInput(text: String) {
                setSlot(
                    2, GuiItems.createGuiItem(
                        name = text,
                        item = Items.PAPER,
                        lore = listOf(
                            Text.empty()
                                .append(Text.literal("→").formatted(Formatting.YELLOW))
                                .append(Text.literal(" Click to confirm").formatted(Formatting.GRAY))
                        )
                    )
                ) { _, _, _, _ ->
                    if (text.isNotEmpty()) {
                        onSubmit(text)
                        close()
                    }
                }
            }
        }

        gui.title = Text.literal(title)
        gui.setSlot(
            0, GuiItems.createGuiItem(
                name = "Cancel",
                item = Items.BARRIER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to cancel").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ ->
            gui.close()
            onCancel()
        }

        gui.setSlot(
            1, GuiItems.createGuiItem(
                name = defaultText,
                item = Items.PAPER,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Type in the text field above").formatted(Formatting.GRAY))
                )
            )
        ) { _, _, _, _ -> }

        gui.open()
    }
}