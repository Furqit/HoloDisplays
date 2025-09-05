package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.utils.GuiUtils
import eu.pb4.sgui.api.elements.GuiElement
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object DisplayList {
    private const val ITEMS_PER_PAGE = 21

    fun open(
        player: ServerPlayerEntity,
        page: Int = 0,
        selectionMode: Boolean = false,
        hologramName: String? = null,
        onSelect: ((String) -> Unit)? = null
    ) {
        val displays = DisplayConfig.getDisplays().toList()
        val pageInfo = GuiUtils.calculatePageInfo(displays.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = GuiUtils.createPagedTitle("Displays", pageInfo),
            size = 45,
            borderSlots = (10..16) + (19..25) + (28..34)
        )

        gui.apply {
            GuiUtils.setupPaginationButtons(
                gui = this,
                pageInfo = pageInfo,
                onPrevious = { open(player, pageInfo.currentPage - 1, selectionMode, hologramName, onSelect) },
                onNext = { open(player, pageInfo.currentPage + 1, selectionMode, hologramName, onSelect) }
            )

            GuiUtils.setupBackButton(this, 40) {
                when {
                    selectionMode && hologramName != null -> HologramDisplays.open(player, hologramName)
                    else -> MainMenu.openMainMenu(player)
                }
            }

            var slot = 10
            val startIndex = pageInfo.currentPage * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, displays.size)

            for (i in startIndex until endIndex) {
                if (slot in listOf(17, 26, 35)) slot += 2

                val (name, display) = displays[i]
                val icon = GuiUtils.getDisplayIcon(display.display)

                val lore = when {
                    selectionMode -> GuiUtils.createCombinedLore(
                        "Type" to display.display.javaClass.simpleName,
                        "Click to select"
                    )

                    else -> GuiUtils.createCombinedLore(
                        "Type" to display.display.javaClass.simpleName,
                        "Left-Click to edit", "Right-Click to delete"
                    )
                }

                setSlot(slot, GuiElement(
                    GuiUtils.createGuiItem(item = icon, name = name, lore = lore)
                ) { _, type, _, _ ->
                    when {
                        selectionMode -> onSelect?.invoke(name)
                        type.isRight -> DeleteConfirmation.open(player, name, "display") {
                            open(player, pageInfo.currentPage)
                        }

                        else -> DisplayEdit.open(player, name)
                    }
                })
                slot++
            }

            open()
        }
    }
}