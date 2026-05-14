package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object CreateDisplay {

    fun open(player: ServerPlayer, hologramName: String? = null) {
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_3x3,
            player = player,
            title = "Create Display",
            size = 9,
            borderSlots = listOf(1, 3, 5, 6, 7)
        )

        gui.apply {
            setSlot(1, GuiUtils.createGuiItem(
                name = "Component Display",
                item = Items.PAPER,
                lore = GuiUtils.createActionLore("Click to create a new text display")
            )) { _, _, _, _ ->
                createDisplay(player, hologramName, "text", "text_display", "Hello World!")
            }

            setSlot(3, GuiUtils.createGuiItem(
                name = "Item Display",
                item = Items.ITEM_FRAME,
                lore = GuiUtils.createActionLore("Click to create a new item display")
            )) { _, _, _, _ ->
                createDisplay(player, hologramName, "item", "item_display", "minecraft:diamond_sword")
            }

            setSlot(5, GuiUtils.createGuiItem(
                name = "Block Display",
                item = Items.GRASS_BLOCK,
                lore = GuiUtils.createActionLore("Click to create a new block display")
            )) { _, _, _, _ ->
                createDisplay(player, hologramName, "block", "block_display", "minecraft:grass_block")
            }

            setSlot(7, GuiUtils.createGuiItem(
                name = "Entity Display",
                item = Items.ARMOR_STAND,
                lore = GuiUtils.createActionLore("Click to create a new entity display")
            )) { _, _, _, _ ->
                createDisplay(player, hologramName, "entity", "entity_display", "minecraft:zombie")
            }

            GuiUtils.setupBackButton(this, 6) {
                when (hologramName) {
                    null -> MainMenu.openMainMenu(player)
                    else -> HologramEdit.open(player, hologramName)
                }
            }

            open()
        }
    }

    private fun createDisplay(player: ServerPlayer, hologramName: String?, type: String, defaultName: String, defaultValue: String) {
        AnvilInput.open(
            player = player,
            title = "Enter Display Name",
            defaultText = defaultName,
            onSubmit = { name ->
                val (title, default) = when (type) {
                    "text" -> "Enter Component" to defaultValue
                    "item" -> "Enter Item ID" to defaultValue
                    "block" -> "Enter Block ID" to defaultValue
                    "entity" -> "Enter Entity ID" to defaultValue
                    else -> "Enter Value" to defaultValue
                }

                AnvilInput.open(
                    player = player,
                    title = title,
                    defaultText = default,
                    onSubmit = { value ->
                        val success = when (type) {
                            "text" -> DisplayManager.createTextDisplay(name, value, player.createCommandSourceStack())
                            "item" -> DisplayManager.createItemDisplay(name, value, player.createCommandSourceStack())
                            "block" -> DisplayManager.createBlockDisplay(name, value, player.createCommandSourceStack())
                            "entity" -> DisplayManager.createEntityDisplay(name, value, player.createCommandSourceStack())
                            else -> false
                        }

                        if (success) {
                            if (hologramName != null) {
                                HologramManager.addDisplayToHologram(hologramName, name, player.createCommandSourceStack())
                                DisplayEdit.open(player, name) {
                                    HologramDisplays.open(player, hologramName)
                                }
                            } else {
                                DisplayEdit.open(player, name)
                            }
                        }
                    },
                    onCancel = { open(player, hologramName) }
                )
            },
            onCancel = { open(player, hologramName) }
        )
    }
}