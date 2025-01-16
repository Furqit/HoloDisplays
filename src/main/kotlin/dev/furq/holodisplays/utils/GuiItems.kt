package dev.furq.holodisplays.utils

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object GuiItems {
    fun createBackItem() = createGuiItem(
        item = Items.ARROW,
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
}