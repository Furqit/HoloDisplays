package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.utils.GuiUtils
import eu.pb4.sgui.api.elements.GuiElement
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object HologramList {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayerEntity, page: Int = 0) {
        val holograms = HologramConfig.getHolograms().toList()
        val pageInfo = GuiUtils.calculatePageInfo(holograms.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = GuiUtils.createPagedTitle("Holograms", pageInfo),
            size = 45,
            borderSlots = (10..16) + (19..25) + (28..34)
        )

        gui.apply {
            GuiUtils.setupPaginationButtons(
                gui = this,
                pageInfo = pageInfo,
                onPrevious = { open(player, pageInfo.currentPage - 1) },
                onNext = { open(player, pageInfo.currentPage + 1) }
            )

            GuiUtils.setupBackButton(this, 40) { MainMenu.openMainMenu(player) }

            var slot = 10
            val startIndex = pageInfo.currentPage * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, holograms.size)

            for (i in startIndex until endIndex) {
                if (slot in listOf(17, 26, 35)) slot += 2

                val (name) = holograms[i]
                val lore = GuiUtils.createActionLore("Left-Click to edit", "Right-Click to delete")

                setSlot(slot, GuiElement(
                    GuiUtils.createGuiItem(item = Items.BOOK, name = name, lore = lore)
                ) { _, type, _, _ ->
                    when {
                        type.isRight -> DeleteConfirmation.open(player, name, "hologram") {
                            open(player, pageInfo.currentPage)
                        }

                        else -> HologramEdit.open(player, name)
                    }
                })
                slot++
            }

            open()
        }
    }
}