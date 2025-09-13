package dev.furq.holodisplays.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("POSE")
    static TrackedData<net.minecraft.entity.EntityPose> getPose() {
        throw new AssertionError();
    }

    @Accessor("FLAGS")
    static TrackedData<Byte> getFlags() {
        throw new AssertionError();
    }
}