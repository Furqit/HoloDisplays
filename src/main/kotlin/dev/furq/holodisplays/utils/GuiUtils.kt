package dev.furq.holodisplays.utils

import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object GuiUtils {

    fun createBackItem() = createGuiItem(
        item = Items.BARRIER,
        name = "Back",
        lore = listOf(
            Text.empty()
                .append(Text.literal("→").formatted(Formatting.YELLOW))
                .append(Text.literal(" Click to go back").formatted(Formatting.GRAY))
        )
    )

    fun createBorderItem() = createGuiItem(
        item = Items.GRAY_STAINED_GLASS_PANE,
        name = ""
    )

    fun createGuiItem(
        item: Item,
        name: String,
        lore: List<Text> = emptyList()
    ): ItemStack {
        return ItemStack(item).apply {
            set(
                DataComponentTypes.CUSTOM_NAME,
                Text.literal(name).setStyle(Style.EMPTY.withItalic(false)).formatted(Formatting.GREEN)
            )
            set(DataComponentTypes.LORE, LoreComponent(lore.map {
                it.copy().setStyle(Style.EMPTY.withItalic(false))
            }))
        }
    }

    fun createGui(
        type: ScreenHandlerType<*>,
        player: ServerPlayerEntity,
        title: String,
        size: Int,
        borderSlots: List<Int>
    ): SimpleGui = SimpleGui(type, player, false).apply {
        this.title = Text.literal(title)
        setupBorders(size, borderSlots)
    }

    private fun SimpleGui.setupBorders(totalSlots: Int, excludeSlots: List<Int>) {
        for (i in 0 until totalSlots) {
            if (i !in excludeSlots) {
                setSlot(i, createBorderItem())
            }
        }
    }

    fun createLore(vararg lines: String): List<Text> = lines.map { line ->
        Text.empty().append(Text.literal(line).formatted(Formatting.GRAY))
    }

    fun createCurrentValueLore(label: String, value: String): List<Text> = listOf(
        Text.empty()
            .append(Text.literal("$label: ").formatted(Formatting.GRAY))
            .append(Text.literal(value).formatted(Formatting.WHITE))
    )

    fun createActionLore(vararg actions: String): List<Text> = actions.map { action ->
        Text.empty()
            .append(Text.literal("→").formatted(Formatting.YELLOW))
            .append(Text.literal(" $action").formatted(Formatting.GRAY))
    }

    fun createCombinedLore(currentValue: Pair<String, String>, vararg actions: String): List<Text> =
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
}