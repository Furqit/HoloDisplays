package dev.furq.holodisplays.api;

import dev.furq.holodisplays.HoloDisplays;
import dev.furq.holodisplays.config.DisplayConfig;
import dev.furq.holodisplays.config.HologramConfig;
import dev.furq.holodisplays.data.DisplayData;
import dev.furq.holodisplays.data.HologramData;
import dev.furq.holodisplays.data.display.*;
import dev.furq.holodisplays.handlers.ViewerHandler;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.server.MinecraftServer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public record HoloDisplaysAPIImpl(String modId) implements HoloDisplaysAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoloDisplaysAPIImpl.class);
    private static final Map<String, HologramData> apiHolograms = new HashMap<>();
    private static final Map<String, DisplayData> apiDisplays = new HashMap<>();

    public HoloDisplaysAPIImpl {
        if (modId == null || modId.isEmpty()) {
            throw new IllegalArgumentException("Mod ID cannot be null or empty");
        }
        if ("minecraft".equals(modId)) {
            throw new IllegalArgumentException("Cannot use 'minecraft' as mod ID");
        }
    }

    static boolean hasApiHolograms() {
        return !apiHolograms.isEmpty();
    }

    static void forEachApiHologram(java.util.function.BiConsumer<String, HologramData> consumer) {
        apiHolograms.forEach(consumer);
    }

    static void clearAllStatic() {
        apiHolograms.keySet().forEach(id -> {
            ViewerHandler.INSTANCE.removeHologramFromAllViewers(id);
            ViewerHandler.INSTANCE.removeTracker(id);
            ViewerHandler.INSTANCE.removeHologramIndex(id);
        });
        apiHolograms.clear();
        apiDisplays.clear();
    }

    static DisplayData getDisplayStatic(String id) {
        return apiDisplays.get(id);
    }

    static HologramData getHologramStatic(String id) {
        return apiHolograms.get(id);
    }

    private static BillboardMode parseBillboardMode(String mode) {
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

    @Override
    public boolean registerHologram(String id, HologramData hologram) {
        try {
            String fullId = toFullId(id);

            if (apiHolograms.containsKey(fullId)) {
                throw new IllegalArgumentException("Hologram with ID " + id + " is already registered");
            }

            for (HologramData.DisplayLine display : hologram.getDisplays()) {
                if (!DisplayConfig.INSTANCE.exists(display.getName()) && !apiDisplays.containsKey(display.getName())) {
                    throw new IllegalArgumentException("Display with ID " + display.getName() + " does not exist");
                }
            }

            apiHolograms.put(fullId, hologram);
            ViewerHandler.INSTANCE.createTracker(fullId);
            ViewerHandler.INSTANCE.updateHologramIndex(fullId, hologram.getPosition());

            MinecraftServer server = HoloDisplays.Companion.getSERVER();
            if (server != null && server.getPlayerManager() != null) {
                server.getPlayerManager().getPlayerList().forEach(ViewerHandler.INSTANCE::updatePlayerVisibility);
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to register hologram {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unregisterHologram(String id) {
        try {
            String fullId = toFullId(id);

            if (!apiHolograms.containsKey(fullId)) {
                return false;
            }

            ViewerHandler.INSTANCE.removeHologramFromAllViewers(fullId);
            ViewerHandler.INSTANCE.removeTracker(fullId);
            ViewerHandler.INSTANCE.removeHologramIndex(fullId);
            apiHolograms.remove(fullId);

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to unregister hologram {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateHologram(String id, HologramData hologram) {
        try {
            String fullId = toFullId(id);

            if (!apiHolograms.containsKey(fullId)) {
                return false;
            }

            for (HologramData.DisplayLine display : hologram.getDisplays()) {
                if (!DisplayConfig.INSTANCE.exists(display.getName()) && !apiDisplays.containsKey(display.getName())) {
                    throw new IllegalArgumentException("Display with ID " + display.getName() + " does not exist");
                }
            }

            apiHolograms.put(fullId, hologram);
            ViewerHandler.INSTANCE.updateHologramIndex(fullId, hologram.getPosition());
            ViewerHandler.INSTANCE.respawnForAllObservers(fullId);

            MinecraftServer server = HoloDisplays.Companion.getSERVER();
            if (server != null && server.getPlayerManager() != null) {
                server.getPlayerManager().getPlayerList().forEach(ViewerHandler.INSTANCE::updatePlayerVisibility);
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to update hologram {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHologramRegistered(String id) {
        return apiHolograms.containsKey(toFullId(id));
    }

    @Override
    public DisplayData createTextDisplay(String id, Consumer<TextDisplayBuilder> builder) {
        TextDisplayBuilderImpl textBuilder = new TextDisplayBuilderImpl();
        builder.accept(textBuilder);
        TextDisplay display = textBuilder.build();
        return registerDisplay(id, display);
    }

    @Override
    public DisplayData createItemDisplay(String id, Consumer<ItemDisplayBuilder> builder) {
        ItemDisplayBuilderImpl itemBuilder = new ItemDisplayBuilderImpl();
        builder.accept(itemBuilder);
        ItemDisplay display = itemBuilder.build();
        return registerDisplay(id, display);
    }

    @Override
    public DisplayData createBlockDisplay(String id, Consumer<BlockDisplayBuilder> builder) {
        BlockDisplayBuilderImpl blockBuilder = new BlockDisplayBuilderImpl();
        builder.accept(blockBuilder);
        BlockDisplay display = blockBuilder.build();
        return registerDisplay(id, display);
    }

    @Override
    public DisplayData createEntityDisplay(String id, Consumer<EntityDisplayBuilder> builder) {
        EntityDisplayBuilderImpl entityBuilder = new EntityDisplayBuilderImpl();
        builder.accept(entityBuilder);
        EntityDisplay display = entityBuilder.build();
        return registerDisplay(id, display);
    }

    @Override
    public HologramBuilder createHologramBuilder() {
        return new HologramBuilderImpl(modId);
    }

    @Override
    public int unregisterAllHolograms() {
        String prefix = modId + ":";
        List<String> hologramsToRemove = apiHolograms.keySet().stream()
                .filter(id -> id.startsWith(prefix))
                .toList();

        hologramsToRemove.forEach(id -> {
            ViewerHandler.INSTANCE.removeHologramFromAllViewers(id);
            ViewerHandler.INSTANCE.removeTracker(id);
            ViewerHandler.INSTANCE.removeHologramIndex(id);
            apiHolograms.remove(id);
        });

        return hologramsToRemove.size();
    }

    @Override
    public int unregisterAllDisplays() {
        String prefix = modId + ":";
        List<String> displaysToRemove = apiDisplays.keySet().stream()
                .filter(id -> id.startsWith(prefix))
                .toList();

        displaysToRemove.forEach(id -> {
            List<String> affectedHolograms = findHologramsUsingDisplay(id);
            apiDisplays.remove(id);

            for (String hologramId : affectedHolograms) {
                if (apiHolograms.containsKey(hologramId)) {
                    ViewerHandler.INSTANCE.respawnForAllObservers(hologramId);
                }
            }
        });

        return displaysToRemove.size();
    }

    @Override
    public DisplayData getDisplay(String id) {
        return apiDisplays.get(toFullId(id));
    }

    @Override
    public HologramData getHologram(String id) {
        return apiHolograms.get(toFullId(id));
    }

    @Override
    public boolean isDisplayRegistered(String id) {
        return apiDisplays.containsKey(toFullId(id));
    }

    @Override
    public boolean updateDisplay(String id, DisplayData display) {
        try {
            String fullId = toFullId(id);

            if (!apiDisplays.containsKey(fullId) || display == null) {
                return false;
            }

            DisplayData oldDisplay = apiDisplays.get(fullId);
            apiDisplays.put(fullId, display);
            updateAffectedHolograms(fullId, oldDisplay, display);

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to update display {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unregisterDisplay(String id) {
        try {
            String fullId = toFullId(id);

            if (!apiDisplays.containsKey(fullId)) {
                return false;
            }

            List<String> affectedHolograms = findHologramsUsingDisplay(fullId);
            apiDisplays.remove(fullId);

            for (String hologramId : affectedHolograms) {
                if (apiHolograms.containsKey(hologramId)) {
                    ViewerHandler.INSTANCE.respawnForAllObservers(hologramId);
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to unregister display {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public void clearAll() {
        apiHolograms.keySet().forEach(id -> {
            ViewerHandler.INSTANCE.removeHologramFromAllViewers(id);
            ViewerHandler.INSTANCE.removeTracker(id);
            ViewerHandler.INSTANCE.removeHologramIndex(id);
        });
        apiHolograms.clear();
        apiDisplays.clear();
    }

    private String toFullId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        return id.contains(":") ? id : modId + ":" + id;
    }

    private DisplayData registerDisplay(String id, BaseDisplay display) {
        String fullId = toFullId(id);

        if (apiDisplays.containsKey(fullId)) {
            throw new IllegalArgumentException("Display with ID " + id + " is already registered");
        }

        DisplayData displayData = new DisplayData(display);
        apiDisplays.put(fullId, displayData);

        return displayData;
    }

    private List<String> findHologramsUsingDisplay(String displayId) {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, HologramData> entry : apiHolograms.entrySet()) {
            if (entry.getValue().getDisplays().stream()
                    .anyMatch(line -> line.getName().equals(displayId))) {
                result.add(entry.getKey());
            }
        }

        if (apiDisplays.containsKey(displayId)) {
            for (Map.Entry<String, HologramData> entry : HologramConfig.INSTANCE.getHolograms().entrySet()) {
                if (entry.getValue().getDisplays().stream()
                        .anyMatch(line -> line.getName().equals(displayId))) {
                    result.add(entry.getKey());
                }
            }
        }

        return result;
    }

    private boolean requiresRespawn(DisplayData oldDisplay, DisplayData newDisplay) {
        BaseDisplay oldType = oldDisplay.getType();
        BaseDisplay newType = newDisplay.getType();

        if (!oldType.getClass().equals(newType.getClass())) {
            return true;
        }

        return switch (oldType) {
            case TextDisplay ignored -> !Objects.equals(oldType.getRotation(), newType.getRotation()) ||
                    !Objects.equals(oldType.getLeftRotation(), newType.getLeftRotation()) ||
                    !Objects.equals(oldType.getRightRotation(), newType.getRightRotation()) ||
                    !Objects.equals(oldType.getConditionalPlaceholder(), newType.getConditionalPlaceholder());
            case ItemDisplay oldItem -> {
                ItemDisplay newItem = (ItemDisplay) newType;
                yield !oldItem.getId().equals(newItem.getId()) ||
                        !Objects.equals(oldType.getRotation(), newType.getRotation()) ||
                        !Objects.equals(oldType.getLeftRotation(), newType.getLeftRotation()) ||
                        !Objects.equals(oldType.getRightRotation(), newType.getRightRotation()) ||
                        !Objects.equals(oldType.getConditionalPlaceholder(), newType.getConditionalPlaceholder());
            }
            case BlockDisplay oldBlock -> {
                BlockDisplay newBlock = (BlockDisplay) newType;
                yield !oldBlock.getId().equals(newBlock.getId()) ||
                        !Objects.equals(oldType.getRotation(), newType.getRotation()) ||
                        !Objects.equals(oldType.getLeftRotation(), newType.getLeftRotation()) ||
                        !Objects.equals(oldType.getRightRotation(), newType.getRightRotation()) ||
                        !Objects.equals(oldType.getConditionalPlaceholder(), newType.getConditionalPlaceholder());
            }
            case EntityDisplay oldEntity -> {
                EntityDisplay newEntity = (EntityDisplay) newType;
                yield !oldEntity.getId().equals(newEntity.getId()) ||
                        !Objects.equals(oldType.getScale(), newType.getScale()) ||
                        !Objects.equals(oldType.getRotation(), newType.getRotation()) ||
                        !Objects.equals(oldType.getLeftRotation(), newType.getLeftRotation()) ||
                        !Objects.equals(oldType.getRightRotation(), newType.getRightRotation()) ||
                        !Objects.equals(oldType.getConditionalPlaceholder(), newType.getConditionalPlaceholder());
            }
            default -> false;
        };
    }

    private void updateAffectedHolograms(String displayId, DisplayData oldDisplay, DisplayData newDisplay) {
        List<String> affectedHolograms = findHologramsUsingDisplay(displayId);
        boolean needsRespawn = requiresRespawn(oldDisplay, newDisplay);

        affectedHolograms.forEach(hologramId -> {
            if (needsRespawn) {
                ViewerHandler.INSTANCE.respawnForAllObservers(hologramId);
            } else {
                ViewerHandler.INSTANCE.updateForAllObservers(hologramId);
            }
        });
    }

    private abstract static class BaseDisplayBuilderImpl<T extends BaseDisplay.Builder<?>> {
        protected final T builder;

        protected BaseDisplayBuilderImpl(T builder) {
            this.builder = builder;
        }

        public void scale(float x, float y, float z) {
            builder.setScale(new Vector3f(x, y, z));
        }

        public void rotation(float x, float y, float z) {
            builder.setRotation(new Vector3f(x, y, z));
        }

        public void leftRotation(float x, float y, float z, float w) {
            builder.setLeftRotation(new Quaternionf(x, y, z, w));
        }

        public void rightRotation(float x, float y, float z, float w) {
            builder.setRightRotation(new Quaternionf(x, y, z, w));
        }

        public void billboardMode(String mode) {
            builder.setBillboardMode(parseBillboardMode(mode));
        }

        public void condition(String placeholder) {
            builder.setConditionalPlaceholder(placeholder);
        }
    }

    private static class TextDisplayBuilderImpl extends BaseDisplayBuilderImpl<TextDisplay.Builder> implements TextDisplayBuilder {
        public TextDisplayBuilderImpl() {
            super(new TextDisplay.Builder());
        }

        @Override
        public void text(String... lines) {
            builder.setLines(lines == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(lines)));
        }

        @Override
        public void backgroundColor(String hexColor, int opacity) {
            if (hexColor == null || !hexColor.matches("^[0-9A-Fa-f]{6}$")) {
                throw new IllegalArgumentException("Color must be a valid 6-digit hexadecimal color code");
            }
            if (opacity < 0 || opacity > 100) {
                throw new IllegalArgumentException("Opacity must be between 0 and 100");
            }

            int opacityValue = (int) (opacity / 100.0 * 255);
            String opacityHex = String.format("%02X", opacityValue);
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

    private static class ItemDisplayBuilderImpl extends BaseDisplayBuilderImpl<ItemDisplay.Builder> implements ItemDisplayBuilder {
        public ItemDisplayBuilderImpl() {
            super(new ItemDisplay.Builder());
        }

        @Override
        public void item(String itemId) {
            if (itemId == null || itemId.isEmpty()) {
                throw new IllegalArgumentException("Item ID cannot be null or empty");
            }
            builder.setId(itemId);
        }

        public ItemDisplay build() {
            return builder.build();
        }
    }

    private static class BlockDisplayBuilderImpl extends BaseDisplayBuilderImpl<BlockDisplay.Builder> implements BlockDisplayBuilder {
        public BlockDisplayBuilderImpl() {
            super(new BlockDisplay.Builder());
        }

        @Override
        public void block(String blockId) {
            if (blockId == null || blockId.isEmpty()) {
                throw new IllegalArgumentException("Block ID cannot be null or empty");
            }
            builder.setId(blockId);
        }

        @Override
        public void properties(Map<String, String> properties) {
            builder.setProperties(properties == null ? new HashMap<>() : properties);
        }

        public BlockDisplay build() {
            return builder.build();
        }
    }

    private static class EntityDisplayBuilderImpl extends BaseDisplayBuilderImpl<EntityDisplay.Builder> implements EntityDisplayBuilder {
        public EntityDisplayBuilderImpl() {
            super(new EntityDisplay.Builder());
        }

        @Override
        public void entity(String entityId) {
            if (entityId == null || entityId.isEmpty()) {
                throw new IllegalArgumentException("Entity ID cannot be null or empty");
            }
            builder.setId(entityId);
        }

        @Override
        public void glow(boolean glow) {
            builder.setGlow(glow);
        }

        @Override
        public void pose(String pose) {
            if (pose == null || pose.isEmpty()) {
                throw new IllegalArgumentException("Entity pose cannot be null or empty");
            }
            try {
                builder.setPose(EntityPose.valueOf(pose.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid entity pose: " + pose);
            }
        }

        public EntityDisplay build() {
            return builder.build();
        }
    }

    private static class HologramBuilderImpl implements HologramBuilder {
        private final String modId;
        private final List<HologramData.DisplayLine> displays = new ArrayList<>();
        private HologramData.Position position = new HologramData.Position("minecraft:overworld", 0.0f, 0.0f, 0.0f);
        private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
        private BillboardMode billboardMode = BillboardMode.CENTER;
        private int updateRate = 20;
        private double viewRange = 48.0;
        private Vector3f rotation = new Vector3f();
        private Quaternionf leftRotation = null;
        private Quaternionf rightRotation = null;
        private String conditionalPlaceholder = null;

        HologramBuilderImpl(String modId) {
            this.modId = modId;
        }

        @Override
        public HologramBuilder position(float x, float y, float z) {
            position = new HologramData.Position(position.getWorld(), x, y, z);
            return this;
        }

        @Override
        public HologramBuilder world(String worldId) {
            if (worldId == null || worldId.isEmpty()) {
                throw new IllegalArgumentException("World ID cannot be null or empty");
            }
            position = new HologramData.Position(worldId, position.getX(), position.getY(), position.getZ());
            return this;
        }

        @Override
        public HologramBuilder scale(float x, float y, float z) {
            scale = new Vector3f(x, y, z);
            return this;
        }

        @Override
        public HologramBuilder billboardMode(String mode) {
            billboardMode = parseBillboardMode(mode);
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
        public HologramBuilder leftRotation(float x, float y, float z, float w) {
            leftRotation = new Quaternionf(x, y, z, w);
            return this;
        }

        @Override
        public HologramBuilder rightRotation(float x, float y, float z, float w) {
            rightRotation = new Quaternionf(x, y, z, w);
            return this;
        }

        @Override
        public HologramBuilder condition(String placeholder) {
            conditionalPlaceholder = placeholder;
            return this;
        }

        @Override
        public HologramBuilder addDisplay(String displayId, float offsetX, float offsetY, float offsetZ) {
            if (displayId == null || displayId.isEmpty()) {
                throw new IllegalArgumentException("Display ID cannot be null or empty");
            }
            String fullId = displayId.contains(":") ? displayId : modId + ":" + displayId;
            displays.add(new HologramData.DisplayLine(fullId, new Vector3f(offsetX, offsetY, offsetZ)));
            return this;
        }

        @Override
        public HologramData build() {
            return new HologramData(
                    displays,
                    position,
                    rotation,
                    leftRotation,
                    rightRotation,
                    scale,
                    billboardMode,
                    updateRate,
                    viewRange,
                    conditionalPlaceholder
            );
        }
    }
}