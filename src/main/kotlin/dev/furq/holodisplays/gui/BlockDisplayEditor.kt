package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object BlockDisplayEditor {

    fun open(
        player: ServerPlayer,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? BlockDisplay ?: return
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_3x3,
            player = player,
            title = "Edit Block Display",
            size = 9,
            borderSlots = listOf(3, 5, 7)
        )

        gui.apply {
            setSlot(3, GuiUtils.createGuiItem(
                name = "Block",
                item = Items.GRASS_BLOCK,
                lore = GuiUtils.createCombinedLore("Current" to display.id, "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Block ID",
                    defaultText = display.id,
                    onSubmit = { blockId ->
                        DisplayManager.updateBlockId(name, blockId, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(5, GuiUtils.createGuiItem(
                name = "Properties",
                item = Items.COMPARATOR,
                lore = GuiUtils.createCombinedLore("Current" to display.properties.toString(), "Click to change", "Format: key=value,key2=value2")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Properties",
                    defaultText = display.properties.entries.joinToString(",") { "${it.key}=${it.value}" },
                    onSubmit = { input ->
                        val properties = input.split(",")
                            .mapNotNull {
                                val parts = it.split("=")
                                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                            }.toMap()
                        DisplayManager.updateBlockProperties(name, properties, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            GuiUtils.setupBackButton(this, 7) { returnCallback() }
            open()
        }
    }
}