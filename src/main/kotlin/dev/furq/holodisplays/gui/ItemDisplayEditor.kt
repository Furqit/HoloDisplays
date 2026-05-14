package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object ItemDisplayEditor {

    private val displayModes = listOf(
        "none", "thirdperson_lefthand", "thirdperson_righthand",
        "firstperson_lefthand", "firstperson_righthand",
        "head", "gui", "ground", "fixed"
    )

    fun open(
        player: ServerPlayer,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? ItemDisplay ?: return
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_3x3,
            player = player,
            title = "Edit Item Display",
            size = 9,
            borderSlots = listOf(1, 3, 5, 6)
        )

        gui.apply {
            setSlot(1, GuiUtils.createGuiItem(
                name = "Item",
                item = Items.ITEM_FRAME,
                lore = GuiUtils.createCombinedLore("Current" to display.id, "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Item ID",
                    defaultText = display.id,
                    onSubmit = { itemId ->
                        DisplayManager.updateItemId(name, itemId, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(3, GuiUtils.createGuiItem(
                name = "Display Type",
                item = Items.ARMOR_STAND,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Current", display.itemDisplayType.uppercase()))
                    addAll(GuiUtils.createActionLore("Click to cycle through modes"))
                    addAll(GuiUtils.createLore(
                        "Available modes:",
                        "NONE, HEAD, GUI, GROUND, FIXED",
                        "THIRDPERSON (LEFT/RIGHT)",
                        "FIRSTPERSON (LEFT/RIGHT)"
                    ))
                }
            )) { _, _, _, _ ->
                val currentIndex = displayModes.indexOf(display.itemDisplayType.lowercase())
                val nextMode = displayModes[(currentIndex + 1) % displayModes.size]
                DisplayManager.updateItemDisplayType(name, nextMode, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            setSlot(5, GuiUtils.createGuiItem(
                name = "Custom Model Data",
                item = Items.COMMAND_BLOCK,
                lore = GuiUtils.createCombinedLore(
                    "Current" to (display.customModelData?.toString() ?: "none"),
                    "Click to change", "Right-click to remove"
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> AnvilInput.open(
                        player = player,
                        title = "Enter Custom Model Data",
                        defaultText = display.customModelData?.toString() ?: "1",
                        onSubmit = { input ->
                            input.toIntOrNull()?.takeIf { it > 0 }?.let { cmd ->
                                DisplayManager.updateCustomModelData(name, cmd, player.createCommandSourceStack())
                            }
                            open(player, name, returnCallback)
                        },
                        onCancel = { open(player, name, returnCallback) }
                    )

                    type.isRightClick() -> {
                        DisplayManager.updateCustomModelData(name, null, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    }
                }
            }

            GuiUtils.setupBackButton(this, 6) { returnCallback() }
            open()
        }
    }
}