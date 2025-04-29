package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiItems
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ItemDisplayEditor {
    private val displayManager = DisplayManager()

    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.display as? ItemDisplay ?: return
        val gui = SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false)
        gui.title = Text.literal("Edit Item Display")

        for (i in 0..8) {
            if (i !in listOf(1, 3, 5, 6)) {
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
                defaultText = display.id,
                onSubmit = { itemId ->
                    displayManager.updateItemId(name, itemId, player.commandSource)
                    open(player, name)
                },
                onCancel = { open(player, name) }
            )
        }

        gui.setSlot(
            3, GuiItems.createGuiItem(
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
            displayManager.updateItemDisplayType(name, nextMode, player.commandSource)
            open(player, name)
        }

        gui.setSlot(
            5, GuiItems.createGuiItem(
                name = "Custom Model Data",
                item = Items.COMMAND_BLOCK,
                lore = listOf(
                    Text.empty()
                        .append(Text.literal("Current: ").formatted(Formatting.GRAY))
                        .append(
                            Text.literal(display.customModelData?.toString() ?: "none").formatted(Formatting.WHITE)
                        ),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Click to change").formatted(Formatting.GRAY)),
                    Text.empty()
                        .append(Text.literal("→").formatted(Formatting.YELLOW))
                        .append(Text.literal(" Right-click to remove").formatted(Formatting.GRAY))
                )
            )
        ) { _, type, _, _ ->
            if (type.isLeft) {
                AnvilInput.open(
                    player = player,
                    title = "Enter Custom Model Data",
                    defaultText = display.customModelData?.toString() ?: "1",
                    onSubmit = { input ->
                        val cmd = input.toIntOrNull()
                        if (cmd != null && cmd > 0) {
                            displayManager.updateCustomModelData(name, cmd, player.commandSource)
                        }
                        open(player, name)
                    },
                    onCancel = { open(player, name) }
                )
            } else if (type.isRight) {
                displayManager.updateCustomModelData(name, null, player.commandSource)
                open(player, name)
            }
        }

        gui.setSlot(6, GuiItems.createBackItem()) { _, _, _, _ ->
            DisplayEdit.open(player, name, returnCallback)
        }

        gui.open()
    }
}