package dev.furq.holodisplays.utils

import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

object GuiUtils {

    fun createBackItem() = createGuiItem(
        item = Items.BARRIER,
        name = "Back",
        lore = listOf(
            Component.empty()
                .append(Component.literal("→").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" Click to go back").withStyle(ChatFormatting.GRAY))
        )
    )

    fun createBorderItem() = createGuiItem(
        //~ if >=26.2 'GRAY_STAINED_GLASS_PANE' -> 'STAINED_GLASS_PANE.gray'
        item = Items.STAINED_GLASS_PANE.gray,
        name = ""
    )

    fun createGuiItem(
        item: Item,
        name: String,
        lore: List<Component> = emptyList()
    ): ItemStack {
        return ItemStack(item).apply {
            set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name).setStyle(Style.EMPTY.withItalic(false)).withStyle(ChatFormatting.GREEN)
            )
            set(DataComponents.LORE, ItemLore(lore.map {
                it.copy().setStyle(Style.EMPTY.withItalic(false))
            }))
        }
    }

    fun createGui(
        type: MenuType<*>,
        player: ServerPlayer,
        title: String,
        size: Int,
        borderSlots: List<Int>
    ): SimpleGui = SimpleGui(type, player, false).apply {
        this.title = Component.literal(title)
        setupBorders(size, borderSlots)
    }

    private fun SimpleGui.setupBorders(totalSlots: Int, excludeSlots: List<Int>) {
        for (i in 0 until totalSlots) {
            if (i !in excludeSlots) {
                setSlot(i, createBorderItem())
            }
        }
    }

    fun createLore(vararg lines: String): List<Component> = lines.map { line ->
        Component.empty().append(Component.literal(line).withStyle(ChatFormatting.GRAY))
    }

    fun createCurrentValueLore(label: String, value: String): List<Component> = listOf(
        Component.empty()
            .append(Component.literal("$label: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(value).withStyle(ChatFormatting.WHITE))
    )

    fun createActionLore(vararg actions: String): List<Component> = actions.map { action ->
        Component.empty()
            .append(Component.literal("→").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" $action").withStyle(ChatFormatting.GRAY))
    }

    fun createCombinedLore(currentValue: Pair<String, String>, vararg actions: String): List<Component> =
        createCurrentValueLore(currentValue.first, currentValue.second) + createActionLore(*actions)

    fun getDisplayIcon(display: Any?): Item = when (display) {
        is TextDisplay -> Items.PAPER
        is ItemDisplay -> Items.ITEM_FRAME
        is BlockDisplay -> Items.GRASS_BLOCK
        is EntityDisplay -> Items.ARMOR_STAND
        else -> Items.BARRIER
    }

    data class PageInfo(
        val currentPage: Int,
        val maxPages: Int,
        val totalItems: Int,
        val itemsPerPage: Int
    )

    fun calculatePageInfo(totalItems: Int, currentPage: Int, itemsPerPage: Int = 21): PageInfo {
        val maxPages = (totalItems - 1) / itemsPerPage
        return PageInfo(
            currentPage = currentPage.coerceIn(0, maxPages),
            maxPages = maxPages,
            totalItems = totalItems,
            itemsPerPage = itemsPerPage
        )
    }

    fun setupPaginationButtons(
        gui: SimpleGui,
        pageInfo: PageInfo,
        onPrevious: () -> Unit,
        onNext: () -> Unit
    ) {
        if (pageInfo.currentPage > 0) {
            gui.setSlot(39, createGuiItem(
                item = Items.ARROW,
                name = "Previous Page",
                lore = createActionLore("Click to go to page ${pageInfo.currentPage}")
            )) { _, _, _, _ -> onPrevious() }
        }

        if (pageInfo.currentPage < pageInfo.maxPages) {
            gui.setSlot(41, createGuiItem(
                item = Items.ARROW,
                name = "Next Page",
                lore = createActionLore("Click to go to page ${pageInfo.currentPage + 2}")
            )) { _, _, _, _ -> onNext() }
        }
    }

    fun setupBackButton(gui: SimpleGui, slot: Int, onBack: () -> Unit) {
        gui.setSlot(slot, createBackItem()) { _, _, _, _ -> onBack() }
    }

    fun createPagedTitle(baseTitle: String, pageInfo: PageInfo): String =
        "$baseTitle (${pageInfo.currentPage + 1}/${pageInfo.maxPages + 1})"

    fun ClickType.isRightClick(): Boolean {
        //~ if >=26.1 'isRight' -> 'this == ClickType.MOUSE_RIGHT'
        return this == ClickType.MOUSE_RIGHT
    }
}