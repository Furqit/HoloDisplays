package dev.furq.holodisplays.handlers;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public final class BlockStateUtil {
    private BlockStateUtil() {
    }

    public static BlockState withParsedProperty(BlockState state, Property<?> property, String valueString) {
        Optional<?> parsed = property.getValue(valueString);
        return parsed.map(o -> setValueUnchecked(state, property, o)).orElse(state);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState setValueUnchecked(BlockState state, Property<?> property, Object value) {
        return state.setValue((Property<T>) property, (T) value);
    }
}