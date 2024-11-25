package dev.furq.holodisplays.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextDisplayEntity.class)
public interface TextDisplayEntityAccessor {
    @Accessor("TEXT")
    static TrackedData<Text> getText() {
        throw new AssertionError();
    }

    @Accessor("LINE_WIDTH")
    static TrackedData<Integer> getLineWidth() {
        throw new AssertionError();
    }

    @Accessor("TEXT_OPACITY")
    static TrackedData<Byte> getTextOpacity() {
        throw new AssertionError();
    }

    @Accessor("BACKGROUND")
    static TrackedData<Integer> getBackground() {
        throw new AssertionError();
    }

    @Accessor("TEXT_DISPLAY_FLAGS")
    static TrackedData<Byte> getTextDisplayFlags() {
        throw new AssertionError();
    }

    @Accessor("SHADOW_FLAG")
    static byte getShadowFlag() {
        throw new AssertionError();
    }

    @Accessor("SEE_THROUGH_FLAG")
    static byte getSeeThroughFlag() {
        throw new AssertionError();
    }

    @Accessor("DEFAULT_BACKGROUND_FLAG")
    static byte getDefaultBackgroundFlag() {
        throw new AssertionError();
    }

    @Accessor("LEFT_ALIGNMENT_FLAG")
    static byte getLeftAlignmentFlag() {
        throw new AssertionError();
    }

    @Accessor("RIGHT_ALIGNMENT_FLAG")
    static byte getRightAlignmentFlag() {
        throw new AssertionError();
    }
}