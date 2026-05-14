package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import org.joml.Vector3f

object HologramDisplays {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayer, hologramName: String, page: Int = 0) {
        val hologram = HologramConfig.getHologram(hologramName) ?: return
        val pageInfo = GuiUtils.calculatePageInfo(hologram.displays.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = GuiUtils.createPagedTitle("Manage Lines", pageInfo),
            size = 45,
            borderSlots = (10..16) + (19..25) + (28..34)
        )

        gui.apply {
            GuiUtils.setupPaginationButtons(
                gui = this,
                pageInfo = pageInfo,
                onPrevious = { open(player, hologramName, pageInfo.currentPage - 1) },
                onNext = { open(player, hologramName, pageInfo.currentPage + 1) }
            )

            GuiUtils.setupBackButton(this, 40) { HologramEdit.open(player, hologramName) }

            var slot = 10
            val startIndex = pageInfo.currentPage * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, hologram.displays.size)

            for (i in startIndex until endIndex) {
                if (slot in listOf(17, 26, 35)) slot += 2

                val display = hologram.displays[i]
                val displayConfig = DisplayConfig.getDisplay(display.name)
                val icon = GuiUtils.getDisplayIcon(displayConfig?.type)

                val lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Display", display.name))
                    addAll(GuiUtils.createCurrentValueLore("Offset", "${display.offset.x}, ${display.offset.y}, ${display.offset.z}"))
                    addAll(GuiUtils.createActionLore(
                        "Left-Click to edit offset",
                        "Right-Click to remove",
                        "Middle-Click to edit display"
                    ))
                }

                setSlot(slot, GuiUtils.createGuiItem(
                    name = "Line ${i + 1}",
                    item = icon,
                    lore = lore
                )) { _, type, _, _ ->
                    when {
                        type.isMiddle -> DisplayEdit.open(player, display.name) {
                            open(player, hologramName, pageInfo.currentPage)
                        }

                        type.isRightClick() -> {
                            if (HologramManager.removeDisplayFromHologram(hologramName, display.name, player.createCommandSourceStack())) {
                                open(player, hologramName, pageInfo.currentPage)
                            }
                        }

                        else -> editOffset(player, hologramName, i, display.offset, pageInfo.currentPage)
                    }
                }
                slot++
            }

            setSlot(43, GuiUtils.createGuiItem(
                name = "Add Line",
                item = Items.EMERALD,
                lore = GuiUtils.createActionLore(
                    "Left-Click to create new display",
                    "Right-Click to use existing display"
                )
            )) { _, type, _, _ ->
                when {
                    type.isRightClick() -> DisplayList.open(
                        player = player,
                        selectionMode = true,
                        hologramName = hologramName,
                        onSelect = { displayName ->
                            if (HologramManager.addDisplayToHologram(hologramName, displayName, player.createCommandSourceStack())) {
                                open(player, hologramName, pageInfo.currentPage)
                            }
                        }
                    )

                    else -> CreateDisplay.open(player, hologramName)
                }
            }

            open()
        }
    }

    private fun editOffset(player: ServerPlayer, hologramName: String, lineIndex: Int, currentOffset: Vector3f, currentPage: Int) {
        AnvilInput.open(player, "Enter X Offset", currentOffset.x.toString(),
            onSubmit = { x ->
                AnvilInput.open(player, "Enter Y Offset", currentOffset.y.toString(),
                    onSubmit = { y ->
                        AnvilInput.open(player, "Enter Z Offset", currentOffset.z.toString(),
                            onSubmit = { z ->
                                HologramHandler.updateHologramProperty(
                                    hologramName,
                                    HologramHandler.HologramProperty.LineOffset(
                                        lineIndex,
                                        Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
                                    )
                                )
                                open(player, hologramName, currentPage)
                            },
                            onCancel = { open(player, hologramName, currentPage) }
                        )
                    },
                    onCancel = { open(player, hologramName, currentPage) }
                )
            },
            onCancel = { open(player, hologramName, currentPage) }
        )
    }
}