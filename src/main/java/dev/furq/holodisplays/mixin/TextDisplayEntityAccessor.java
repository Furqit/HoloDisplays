package dev.furq.holodisplays.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display.TextDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextDisplay.class)
public interface TextDisplayEntityAccessor {
    @Accessor("DATA_TEXT_ID")
    static EntityDataAccessor<Component> getText() {
        throw new AssertionError();
    }

    @Accessor("DATA_LINE_WIDTH_ID")
    static EntityDataAccessor<Integer> getLineWidth() {
        throw new AssertionError();
    }

    @Accessor("DATA_TEXT_OPACITY_ID")
    static EntityDataAccessor<Byte> getTextOpacity() {
        throw new AssertionError();
    }

    @Accessor("DATA_BACKGROUND_COLOR_ID")
    static EntityDataAccessor<Integer> getBackground() {
        throw new AssertionError();
    }

    @Accessor("DATA_STYLE_FLAGS_ID")
    static EntityDataAccessor<Byte> getTextDisplayFlags() {
        throw new AssertionError();
    }

    @Accessor("FLAG_SHADOW")
    static byte getShadowFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_SEE_THROUGH")
    static byte getSeeThroughFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_USE_DEFAULT_BACKGROUND")
    static byte getDefaultBackgroundFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_ALIGN_LEFT")
    static byte getLeftAlignmentFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_ALIGN_RIGHT")
    static byte getRightAlignmentFlag() {
        throw new AssertionError();
    }
}