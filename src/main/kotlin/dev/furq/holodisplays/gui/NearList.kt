package dev.furq.holodisplays.gui

import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object NearList {
    private const val ITEMS_PER_PAGE = 21

    fun open(player: ServerPlayer, holograms: List<Pair<String, Double>>, page: Int = 0) {
        val pageInfo = GuiUtils.calculatePageInfo(holograms.size, page, ITEMS_PER_PAGE)

        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = GuiUtils.createPagedTitle("Nearby Holograms", pageInfo),
            size = 45,
            borderSlots = (10..16) + (19..25) + (28..34)
        )

        gui.apply {
            GuiUtils.setupPaginationButtons(
                gui = this,
                pageInfo = pageInfo,
                onPrevious = { open(player, holograms, pageInfo.currentPage - 1) },
                onNext = { open(player, holograms, pageInfo.currentPage + 1) }
            )
            GuiUtils.setupBackButton(this, 40) { MainMenu.openMainMenu(player) }

            var slot = 10
            val startIndex = pageInfo.currentPage * ITEMS_PER_PAGE
            val endIndex = minOf(startIndex + ITEMS_PER_PAGE, holograms.size)

            for (i in startIndex until endIndex) {
                if (slot in listOf(17, 26, 35)) slot += 2

                val (name, distance) = holograms[i]
                val lore = listOf(
                    Component.literal("Distance: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("${"%.1f".format(distance)}m").withStyle(ChatFormatting.YELLOW)),
                    Component.empty()
                ) + GuiUtils.createActionLore("Left-Click to edit", "Right-Click to delete")

                setSlot(slot, GuiUtils.createGuiItem(item = Items.BOOK, name = name, lore = lore)) { _, type, _, _ ->
                    when {
                        type.isRightClick() -> DeleteConfirmation.open(player, name, "hologram") {
                            val updatedList = holograms.filter { it.first != name }
                            open(player, updatedList, pageInfo.currentPage)
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
