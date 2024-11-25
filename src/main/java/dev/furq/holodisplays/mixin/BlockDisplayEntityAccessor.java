package dev.furq.holodisplays.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockDisplayEntity.class)
public interface BlockDisplayEntityAccessor {
    @Accessor("BLOCK_STATE")
    static TrackedData<BlockState> getBlockState() {
        throw new AssertionError();
    }
}