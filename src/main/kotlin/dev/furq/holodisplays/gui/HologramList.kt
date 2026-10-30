package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object HologramList {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayer, page: Int = 0) {
        val holograms = HologramConfig.getHolograms().toList()
        val pageInfo = GuiUtils.calculatePageInfo(holograms.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = GuiUtils.createPagedTitle(Lang.get(player, "gui.hologram_list.title", "Holograms"), pageInfo),
            size = 45,
            borderSlots = GuiUtils.PAGED_BORDER_SLOTS
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
                slot = GuiUtils.advanceContentSlot(slot)

                val (name) = holograms[i]
                val lore = GuiUtils.createActionLore(
                    Lang.get(player, "gui.common.left_click_edit", "Left-Click to edit"),
                    Lang.get(player, "gui.common.right_click_delete", "Right-Click to delete")
                )

                setSlot(slot, GuiUtils.createGuiItem(item = Items.BOOK, name = name, lore = lore)) { _, type, _, _ ->
                    when {
                        type.isRightClick() -> DeleteConfirmation.open(player, name, "hologram") {
                            open(player, pageInfo.currentPage)
                        }

                        else -> HologramEdit.open(player, name)
                    }
                }
                slot++
            }

            open()
        }
    }
}