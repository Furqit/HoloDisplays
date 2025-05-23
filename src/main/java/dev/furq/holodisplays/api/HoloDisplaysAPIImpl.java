package dev.furq.holodisplays.api;

import dev.furq.holodisplays.HoloDisplays;
import dev.furq.holodisplays.config.DisplayConfig;
import dev.furq.holodisplays.data.DisplayData;
import dev.furq.holodisplays.data.HologramData;
import dev.furq.holodisplays.data.display.BaseDisplay;
import dev.furq.holodisplays.data.display.BlockDisplay;
import dev.furq.holodisplays.data.display.ItemDisplay;
import dev.furq.holodisplays.data.display.TextDisplay;
import dev.furq.holodisplays.handlers.ViewerHandler;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.server.MinecraftServer;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class HoloDisplaysAPIImpl implements HoloDisplaysAPI {

    public static final HoloDisplaysAPIImpl INSTANCE = new HoloDisplaysAPIImpl();
    public final Map<String, HologramData> apiHolograms = new HashMap<>();
    private final Map<String, DisplayData> apiDisplays = new HashMap<>();

    private HoloDisplaysAPIImpl() {
    }

    @Override
    public boolean registerHologram(String id, HologramData hologram) {
        try {
            String stringId = validateId(id);

            if (apiHolograms.containsKey(stringId)) {
                throw new IllegalArgumentException("Hologram with ID " + id + " is already registered");
            }

            for (HologramData.DisplayLine display : hologram.getDisplays()) {
                if (!DisplayConfig.INSTANCE.exists(display.getDisplayId()) && !apiDisplays.containsKey(display.getDisplayId())) {
                    throw new IllegalArgumentException("Display with ID " + display.getDisplayId() + " does not exist");
                }
            }

            apiHolograms.put(stringId, hologram);
            ViewerHandler.INSTANCE.createTracker(stringId, hologram);

            MinecraftServer server = HoloDisplays.Companion.getSERVER();
            if (server != null && server.getPlayerManager() != null) {
                server.getPlayerManager().getPlayerList().forEach(ViewerHandler.INSTANCE::updatePlayerVisibility);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unregisterHologram(String id) {
        try {
            String stringId = validateId(id);

            if (!apiHolograms.containsKey(stringId)) {
                return false;
            }

            ViewerHandler.INSTANCE.removeHologramFromAllViewers(stringId);
            ViewerHandler.INSTANCE.removeTracker(stringId);
            apiHolograms.remove(stringId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateHologram(String id, HologramData hologram) {
        try {
            String stringId = validateId(id);

            if (!apiHolograms.containsKey(stringId)) {
                return false;
            }

            for (HologramData.DisplayLine display : hologram.getDisplays()) {
                if (!DisplayConfig.INSTANCE.exists(display.getDisplayId()) && !apiDisplays.containsKey(display.getDisplayId())) {
                    throw new IllegalArgumentException("Display with ID " + display.getDisplayId() + " does not exist");
                }
            }

            apiHolograms.put(stringId, hologram);
            ViewerHandler.INSTANCE.respawnForAllObservers(stringId);

            MinecraftServer server = HoloDisplays.Companion.getSERVER();
            if (server != null && server.getPlayerManager() != null) {
                server.getPlayerManager().getPlayerList().forEach(ViewerHandler.INSTANCE::updatePlayerVisibility);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isHologramRegistered(String id) {
        try {
            String stringId = validateId(id);
            return apiHolograms.containsKey(stringId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public DisplayData createTextDisplay(String id, Consumer<TextDisplayBuilder> builder) {
        String stringId = validateId(id);
        TextDisplayBuilderImpl textBuilder = new TextDisplayBuilderImpl();
        builder.accept(textBuilder);
        TextDisplay display = textBuilder.build();
        apiDisplays.put(stringId, new DisplayData(display));
        return apiDisplays.get(stringId);
    }

    @Override
    public DisplayData createItemDisplay(String id, Consumer<ItemDisplayBuilder> builder) {
        String stringId = validateId(id);
        ItemDisplayBuilderImpl itemBuilder = new ItemDisplayBuilderImpl();
        builder.accept(itemBuilder);
        ItemDisplay display = itemBuilder.build();
        apiDisplays.put(stringId, new DisplayData(display));
        return apiDisplays.get(stringId);
    }

    @Override
    public DisplayData createBlockDisplay(String id, Consumer<BlockDisplayBuilder> builder) {
        String stringId = validateId(id);
        BlockDisplayBuilderImpl blockBuilder = new BlockDisplayBuilderImpl();
        builder.accept(blockBuilder);
        BlockDisplay display = blockBuilder.build();
        apiDisplays.put(stringId, new DisplayData(display));
        return apiDisplays.get(stringId);
    }

    @Override
    public HologramBuilder createHologramBuilder() {
        return new HologramBuilderImpl();
    }

    @Override
    public int unregisterAllHolograms(String namespace) {
        if ("minecraft".equals(namespace)) {
            throw new IllegalArgumentException("Cannot use 'minecraft' namespace for custom holograms");
        }

        List<String> hologramsToRemove = new ArrayList<>();
        for (String id : apiHolograms.keySet()) {
            if (id.startsWith(namespace + ":")) {
                hologramsToRemove.add(id);
            }
        }

        for (String id : hologramsToRemove) {
            ViewerHandler.INSTANCE.removeHologramFromAllViewers(id);
            ViewerHandler.INSTANCE.removeTracker(id);
            apiHolograms.remove(id);
        }

        return hologramsToRemove.size();
    }

    @Override
    public DisplayData getDisplay(String id) {
        return apiDisplays.get(id);
    }

    @Override
    public void clearAll() {
        List<String> hologramIds = new ArrayList<>(apiHolograms.keySet());
        for (String id : hologramIds) {
            ViewerHandler.INSTANCE.removeHologramFromAllViewers(id);
            ViewerHandler.INSTANCE.removeTracker(id);
        }
        apiHolograms.clear();
        apiDisplays.clear();
    }

    private String validateId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        String[] parts = id.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("ID must include namespace (e.g., 'mymod:my_hologram')");
        }

        if ("minecraft".equals(parts[0])) {
            throw new IllegalArgumentException("Cannot use 'minecraft' namespace for custom holograms");
        }

        return id;
    }

    private BillboardMode parseBillboardMode(String mode) {
        if (mode == null) {
            return BillboardMode.CENTER;
        }

        return switch (mode.toLowerCase()) {
            case "fixed" -> BillboardMode.FIXED;
            case "horizontal" -> BillboardMode.HORIZONTAL;
            case "vertical" -> BillboardMode.VERTICAL;
            default -> BillboardMode.CENTER;
        };
    }

    private abstract static class BaseDisplayBuilder<T extends BaseDisplay.Builder<?>> {
        protected final T builder;

        protected BaseDisplayBuilder(T builder) {
            this.builder = builder;
        }

        public void scale(float x, float y, float z) {
            builder.setScale(new Vector3f(x, y, z));
        }

        public void rotation(float x, float y, float z) {
            builder.setRotation(new Vector3f(x, y, z));
        }

        public void billboardMode(String mode) {
            builder.setBillboardMode(INSTANCE.parseBillboardMode(mode));
        }

        public void condition(String placeholder) {
            builder.setConditionalPlaceholder(placeholder);
        }
    }

    private static class TextDisplayBuilderImpl extends BaseDisplayBuilder<TextDisplay.Builder> implements TextDisplayBuilder {
        public TextDisplayBuilderImpl() {
            super(new TextDisplay.Builder());
        }

        @Override
        public void text(String... lines) {
            builder.setLines(new ArrayList<>(Arrays.asList(lines)));
        }

        @Override
        public void backgroundColor(String hexColor, int opacity) {
            if (hexColor == null || !hexColor.matches("^[0-9A-Fa-f]{6}$")) {
                throw new IllegalArgumentException("Color must be a valid 6-digit hexadecimal color code (e.g., 'FF0000')");
            }

            if (opacity < 1 || opacity > 100) {
                throw new IllegalArgumentException("Opacity must be between 1 and 100");
            }

            int opacityValue = (int) (opacity / 100.0 * 255);
            String opacityHex = Integer.toString(opacityValue, 16).toUpperCase();
            if (opacityHex.length() == 1) {
                opacityHex = "0" + opacityHex;
            }

            builder.setBackgroundColor(opacityHex + hexColor);
        }

        @Override
        public void shadow(boolean hasShadow) {
            builder.setShadow(hasShadow);
        }

        @Override
        public void seeThrough(boolean seeThrough) {
            builder.setSeeThrough(seeThrough);
        }

        @Override
        public void opacity(float opacity) {
            builder.setTextOpacity((int) (opacity * 255));
        }

        public TextDisplay build() {
            return builder.build();
        }
    }

    private static class ItemDisplayBuilderImpl extends BaseDisplayBuilder<ItemDisplay.Builder> implements ItemDisplayBuilder {
        public ItemDisplayBuilderImpl() {
            super(new ItemDisplay.Builder());
        }

        @Override
        public void item(String itemId) {
            builder.setId(itemId);
        }

        public ItemDisplay build() {
            return builder.build();
        }
    }

    private static class BlockDisplayBuilderImpl extends BaseDisplayBuilder<BlockDisplay.Builder> implements BlockDisplayBuilder {
        public BlockDisplayBuilderImpl() {
            super(new BlockDisplay.Builder());
        }

        @Override
        public void block(String blockId) {
            builder.setId(blockId);
        }

        public BlockDisplay build() {
            return builder.build();
        }
    }

    private static class HologramBuilderImpl implements HologramBuilder {
        private final List<HologramData.DisplayLine> displays = new ArrayList<>();
        private Vector3f position = new Vector3f();
        private String world = "minecraft:overworld";
        private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
        private BillboardMode billboardMode = BillboardMode.CENTER;
        private int updateRate = 20;
        private double viewRange = 48.0;
        private Vector3f rotation = new Vector3f();
        private String conditionalPlaceholder = null;

        @Override
        public HologramBuilder position(float x, float y, float z) {
            position = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public HologramBuilder world(String worldId) {
            world = worldId;
            return this;
        }

        @Override
        public HologramBuilder scale(float x, float y, float z) {
            scale = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public HologramBuilder billboardMode(String mode) {
            billboardMode = INSTANCE.parseBillboardMode(mode);
            return this;
        }

        @Override
        public HologramBuilder updateRate(int ticks) {
            updateRate = ticks;
            return this;
        }

        @Override
        public HologramBuilder viewRange(double range) {
            viewRange = range;
            return this;
        }

        @Override
        public HologramBuilder rotation(float x, float y, float z) {
            rotation = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public HologramBuilder condition(String placeholder) {
            conditionalPlaceholder = placeholder;
            return this;
        }

        @Override
        public HologramBuilder addDisplay(String displayId, float offsetX, float offsetY, float offsetZ) {
            displays.add(new HologramData.DisplayLine(displayId, new Vector3f(offsetX, offsetY, offsetZ)));
            return this;
        }

        @Override
        public HologramData build() {
            return new HologramData(
                    displays,
                    position,
                    world,
                    scale,
                    billboardMode,
                    updateRate,
                    viewRange,
                    rotation,
                    conditionalPlaceholder
            );
        }
    }
}