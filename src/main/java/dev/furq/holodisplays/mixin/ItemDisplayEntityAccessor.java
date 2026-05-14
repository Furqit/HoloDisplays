package dev.furq.holodisplays.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemDisplay.class)
public interface ItemDisplayEntityAccessor {
    @Accessor("DATA_ITEM_STACK_ID")
    static EntityDataAccessor<ItemStack> getItem() {
        throw new AssertionError();
    }

    @Accessor("DATA_ITEM_DISPLAY_ID")
    static EntityDataAccessor<Byte> getItemDisplay() {
        throw new AssertionError();
    }
}