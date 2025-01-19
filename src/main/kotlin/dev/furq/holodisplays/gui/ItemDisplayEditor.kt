package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.utils.GuiItems
import dev.furq.holodisplays.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ItemDisplayEditor {
    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.display as? ItemDisplay ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        gui.title = Text.literal("Edit Item Display")

        for (i in 0..8) {
            if (i !in listOf(1, 4, 6)) {
                gui.setSlot(i, GuiItems.createBorderItem())
            }
        }

        gui.setSlot(
            1, GuiItems.createGuiItem(
                name = "Item",
                item = Items.ITEM_FRAME,
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
                title = "Enter Item ID",
                defaultText = display.id
            ) { itemId ->
                Utils.updateDisplayItem(name, itemId, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(
            4, GuiItems.createGuiItem(
                name = "Display Type",
                item = Items.ARMOR_STAND,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(Text.literal(display.itemDisplayType.uppercase()).formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to cycle through modes").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("Available modes: ").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("NONE, HEAD, GUI, GROUND, FIXED").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("THIRDPERSON (LEFT/RIGHT)").formatted(Formatting.WHITE)),
                    Text.empty()
                        .append(Text.literal("FIRSTPERSON (LEFT/RIGHT)").formatted(Formatting.WHITE))
                )
            )
        ) { _, _, _, _ ->
            val modes = listOf(
                "none",
                "thirdperson_lefthand",
                "thirdperson_righthand",
                "firstperson_lefthand",
                "firstperson_righthand",
                "head",
                "gui",
                "ground",
                "fixed"
            )
            val currentIndex = modes.indexOf(display.itemDisplayType.lowercase())
            val nextMode = modes[(currentIndex + 1) % modes.size]
            Utils.updateItemDisplayType(name, nextMode, player.commandSource)
            open(player, name)
        }

        gui.setSlot(6, GuiItems.createBackItem()) { _, _, _, _ ->
            DisplayEdit.open(player, name, returnCallback)
        }

        gui.open()
    }
}