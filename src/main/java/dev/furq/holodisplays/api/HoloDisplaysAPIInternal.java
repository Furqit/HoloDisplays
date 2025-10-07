package dev.furq.holodisplays.api;

import dev.furq.holodisplays.data.DisplayData;
import dev.furq.holodisplays.data.HologramData;

import java.util.function.BiConsumer;

/**
 * Internal API access for the HoloDisplays mod.
 * This class should NOT be used by external mods - use HoloDisplaysAPI instead.
 */
public final class HoloDisplaysAPIInternal {
    private HoloDisplaysAPIInternal() {
    }

    private static void validateCaller() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Class<?> callerClass = walker.getCallerClass();
        String callerPackage = callerClass.getPackageName();

        if (!callerPackage.startsWith("dev.furq.holodisplays")) {
            throw new SecurityException(
                    "HoloDisplaysAPIInternal is for internal use only. " +
                            "External mods should use HoloDisplaysAPI.get(modId) instead. " +
                            "Caller: " + callerClass.getName()
            );
        }
    }

    public static boolean hasApiHolograms() {
        validateCaller();
        return HoloDisplaysAPIImpl.hasApiHolograms();
    }

    public static void forEachApiHologram(BiConsumer<String, HologramData> consumer) {
        validateCaller();
        HoloDisplaysAPIImpl.forEachApiHologram(consumer);
    }

    public static void clearAll() {
        validateCaller();
        HoloDisplaysAPIImpl.clearAllStatic();
    }

    public static DisplayData getDisplay(String id) {
        validateCaller();
        return HoloDisplaysAPIImpl.getDisplayStatic(id);
    }

    public static HologramData getHologram(String id) {
        validateCaller();
        return HoloDisplaysAPIImpl.getHologramStatic(id);
    }
}
