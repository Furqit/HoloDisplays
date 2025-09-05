package dev.furq.holodisplays.gui

import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object CreateDisplay {
    private val hologramManager = HologramManager()
    private val displayManager = DisplayManager()

    fun open(player: ServerPlayerEntity, hologramName: String? = null) {
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_3X3,
            player = player,
            title = "Create Display",
            size = 9,
            borderSlots = listOf(3, 4, 5, 6)
        )

        gui.apply {
            setSlot(3, GuiUtils.createGuiItem(
                name = "Text Display",
                item = Items.PAPER,
                lore = GuiUtils.createActionLore("Click to create a new text display")
            )) { _, _, _, _ ->
                createDisplay(player, hologramName, "text", "text_display", "Hello World!")
            }

            setSlot(4, GuiUtils.createGuiItem(
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

            GuiUtils.setupBackButton(this, 6) {
                when (hologramName) {
                    null -> MainMenu.openMainMenu(player)
                    else -> HologramEdit.open(player, hologramName)
                }
            }

            open()
        }
    }

    private fun createDisplay(player: ServerPlayerEntity, hologramName: String?, type: String, defaultName: String, defaultValue: String) {
        AnvilInput.open(
            player = player,
            title = "Enter Display Name",
            defaultText = defaultName,
            onSubmit = { name ->
                val (title, default) = when (type) {
                    "text" -> "Enter Text" to defaultValue
                    "item" -> "Enter Item ID" to defaultValue
                    "block" -> "Enter Block ID" to defaultValue
                    else -> "Enter Value" to defaultValue
                }

                AnvilInput.open(
                    player = player,
                    title = title,
                    defaultText = default,
                    onSubmit = { value ->
                        val success = when (type) {
                            "text" -> displayManager.createTextDisplay(name, value, player.commandSource)
                            "item" -> displayManager.createItemDisplay(name, value, player.commandSource)
                            "block" -> displayManager.createBlockDisplay(name, value, player.commandSource)
                            else -> false
                        }

                        if (success) {
                            if (hologramName != null) {
                                hologramManager.addDisplayToHologram(hologramName, name, player.commandSource)
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