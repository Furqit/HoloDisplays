package dev.furq.holodisplays.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockDisplay.class)
public interface BlockDisplayEntityAccessor {
    @Accessor("DATA_BLOCK_STATE_ID")
    static EntityDataAccessor<BlockState> getBlockState() {
        throw new AssertionError();
    }
}