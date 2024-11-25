package dev.furq.holodisplays.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemDisplayEntity.class)
public interface ItemDisplayEntityAccessor {
    @Accessor("ITEM")
    static TrackedData<ItemStack> getItem() {
        throw new AssertionError();
    }
    @Accessor("ITEM_DISPLAY")
    static TrackedData<Byte> getItemDisplay() {
        throw new AssertionError();
    }
}