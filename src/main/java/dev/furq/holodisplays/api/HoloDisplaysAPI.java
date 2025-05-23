package dev.furq.holodisplays.api;

import dev.furq.holodisplays.data.DisplayData;
import dev.furq.holodisplays.data.HologramData;

import java.util.function.Consumer;

/**
 * Java API for the HoloDisplays mod.
 * Allows other mods to create and manage holograms programmatically.
 */
public interface HoloDisplaysAPI {
    /**
     * Gets the instance of the HoloDisplays API.
     * Always use this method to access the API rather than attempting
     * to call methods statically on the interface or directly instantiating
     * an implementation.
     *
     * @return The API instance (singleton)
     */
    static HoloDisplaysAPI get() {
        return HoloDisplaysAPIImpl.INSTANCE;
    }

    /**
     * Clears all API-registered holograms and displays.
     * This is called when the server shuts down or the mod is reloaded.
     * Most mods will not need to call this directly.
     */
    void clearAll();

    /**
     * Registers a hologram with the given ID and data.
     * The hologram will be managed by HoloDisplays but not saved to the config file.
     * The ID must have a namespace other than "minecraft" (e.g., "mymod:my_hologram").
     *
     * @param id       The unique identifier for this hologram (must include namespace)
     * @param hologram The hologram data
     * @return True if registration was successful, false otherwise
     */
    boolean registerHologram(String id, HologramData hologram);

    /**
     * Unregisters a previously registered hologram.
     *
     * @param id The unique identifier of the hologram to unregister
     * @return True if unregistration was successful, false otherwise
     */
    boolean unregisterHologram(String id);

    /**
     * Updates a registered hologram with new data.
     *
     * @param id       The unique identifier of the hologram to update
     * @param hologram The new hologram data
     * @return True if update was successful, false otherwise
     */
    boolean updateHologram(String id, HologramData hologram);

    /**
     * Checks if a hologram with the given ID is registered.
     *
     * @param id The unique identifier to check
     * @return True if a hologram with this ID exists, false otherwise
     */
    boolean isHologramRegistered(String id);

    /**
     * Creates a text display with the given configuration.
     * The ID must have a namespace other than "minecraft" (e.g., "mymod:my_display").
     *
     * @param id      The unique identifier for this display
     * @param builder A consumer to configure the text display
     * @return The created display data
     */
    DisplayData createTextDisplay(String id, Consumer<TextDisplayBuilder> builder);

    /**
     * Creates an item display with the given configuration.
     * The ID must have a namespace other than "minecraft" (e.g., "mymod:my_display").
     *
     * @param id      The unique identifier for this display
     * @param builder A consumer to configure the item display
     * @return The created display data
     */
    DisplayData createItemDisplay(String id, Consumer<ItemDisplayBuilder> builder);

    /**
     * Creates a block display with the given configuration.
     * The ID must have a namespace other than "minecraft" (e.g., "mymod:my_display").
     *
     * @param id      The unique identifier for this display
     * @param builder A consumer to configure the block display
     * @return The created display data
     */
    DisplayData createBlockDisplay(String id, Consumer<BlockDisplayBuilder> builder);

    /**
     * Creates a hologram builder to fluently configure a hologram.
     *
     * @return A new hologram builder
     */
    HologramBuilder createHologramBuilder();

    /**
     * Unregisters all holograms with a specific namespace.
     * This is useful for cleanup when your mod is being unloaded.
     *
     * @param namespace The namespace to unregister holograms for
     * @return The number of holograms that were unregistered
     */
    int unregisterAllHolograms(String namespace);

    /**
     * Gets a display by ID, checking only API-registered displays.
     *
     * @param id The display ID to get
     * @return The display data, or null if not found in API-registered displays
     */
    DisplayData getDisplay(String id);

    /**
     * Builder interface for text displays.
     */
    interface TextDisplayBuilder {
        /**
         * Sets the text content of this display.
         *
         * @param lines The lines of text to display
         */
        void text(String... lines);

        /**
         * Sets the scale of this display.
         *
         * @param x X scale factor
         * @param y Y scale factor
         * @param z Z scale factor
         */
        void scale(float x, float y, float z);

        /**
         * Sets the rotation of this display.
         *
         * @param x X rotation in degrees
         * @param y Y rotation in degrees
         * @param z Z rotation in degrees
         */
        void rotation(float x, float y, float z);

        /**
         * Sets the billboard mode of this display.
         *
         * @param mode The billboard mode (fixed, horizontal, vertical, center)
         */
        void billboardMode(String mode);

        /**
         * Sets the conditional placeholder for this display.
         *
         * @param placeholder The condition or null
         */
        void condition(String placeholder);

        /**
         * Sets the background color of this text display.
         *
         * @param hexColor Hexadecimal color code (e.g., "FF0000" for red)
         * @param opacity  Opacity value from 1 to 100
         */
        void backgroundColor(String hexColor, int opacity);

        /**
         * Sets whether this text display has a shadow.
         *
         * @param hasShadow True if text should have a shadow
         */
        void shadow(boolean hasShadow);

        /**
         * Sets whether this text display has a see-through background.
         *
         * @param seeThrough True if background should be see-through
         */
        void seeThrough(boolean seeThrough);

        /**
         * Sets the text opacity of this display.
         *
         * @param opacity Opacity value from 0.0 to 1.0
         */
        void opacity(float opacity);
    }

    /**
     * Builder interface for item displays.
     */
    interface ItemDisplayBuilder {
        /**
         * Sets the item to display.
         *
         * @param itemId The item ID
         */
        void item(String itemId);

        /**
         * Sets the scale of this display.
         *
         * @param x X scale factor
         * @param y Y scale factor
         * @param z Z scale factor
         */
        void scale(float x, float y, float z);

        /**
         * Sets the rotation of this display.
         *
         * @param x X rotation in degrees
         * @param y Y rotation in degrees
         * @param z Z rotation in degrees
         */
        void rotation(float x, float y, float z);

        /**
         * Sets the billboard mode of this display.
         *
         * @param mode The billboard mode (fixed, horizontal, vertical, center)
         */
        void billboardMode(String mode);

        /**
         * Sets the conditional placeholder for this display.
         *
         * @param placeholder The condition or null
         */
        void condition(String placeholder);
    }

    /**
     * Builder interface for block displays.
     */
    interface BlockDisplayBuilder {
        /**
         * Sets the block to display.
         *
         * @param blockId The block ID
         */
        void block(String blockId);

        /**
         * Sets the scale of this display.
         *
         * @param x X scale factor
         * @param y Y scale factor
         * @param z Z scale factor
         */
        void scale(float x, float y, float z);

        /**
         * Sets the rotation of this display.
         *
         * @param x X rotation in degrees
         * @param y Y rotation in degrees
         * @param z Z rotation in degrees
         */
        void rotation(float x, float y, float z);

        /**
         * Sets the billboard mode of this display.
         *
         * @param mode The billboard mode (fixed, horizontal, vertical, center)
         */
        void billboardMode(String mode);

        /**
         * Sets the conditional placeholder for this display.
         *
         * @param placeholder The condition or null
         */
        void condition(String placeholder);
    }

    /**
     * Builder interface for holograms.
     */
    interface HologramBuilder {
        /**
         * Sets the position of this hologram.
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @return This builder for chaining
         */
        HologramBuilder position(float x, float y, float z);

        /**
         * Sets the world this hologram is in.
         *
         * @param worldId World identifier
         * @return This builder for chaining
         */
        HologramBuilder world(String worldId);

        /**
         * Sets the scale of this hologram.
         *
         * @param x X scale factor
         * @param y Y scale factor
         * @param z Z scale factor
         * @return This builder for chaining
         */
        HologramBuilder scale(float x, float y, float z);

        /**
         * Sets the billboard mode of this hologram.
         *
         * @param mode The billboard mode (fixed, horizontal, vertical, center)
         * @return This builder for chaining
         */
        HologramBuilder billboardMode(String mode);

        /**
         * Sets the update rate of this hologram in ticks.
         *
         * @param ticks Update frequency in ticks
         * @return This builder for chaining
         */
        HologramBuilder updateRate(int ticks);

        /**
         * Sets the view range of this hologram.
         *
         * @param range Maximum distance players can see this hologram
         * @return This builder for chaining
         */
        HologramBuilder viewRange(double range);

        /**
         * Sets the rotation of this hologram.
         *
         * @param x X rotation in degrees
         * @param y Y rotation in degrees
         * @param z Z rotation in degrees
         * @return This builder for chaining
         */
        HologramBuilder rotation(float x, float y, float z);

        /**
         * Sets the conditional placeholder for this hologram.
         *
         * @param placeholder The condition or null
         * @return This builder for chaining
         */
        HologramBuilder condition(String placeholder);

        /**
         * Adds a display to this hologram.
         *
         * @param displayId The ID of the display to add
         * @param offsetX   The X offset of this display from the hologram position
         * @param offsetY   The Y offset of this display from the hologram position
         * @param offsetZ   The Z offset of this display from the hologram position
         * @return This builder for chaining
         */
        HologramBuilder addDisplay(String displayId, float offsetX, float offsetY, float offsetZ);

        /**
         * Adds a display to this hologram at the center (0,0,0).
         *
         * @param displayId The ID of the display to add
         * @return This builder for chaining
         */
        default HologramBuilder addDisplay(String displayId) {
            return addDisplay(displayId, 0f, 0f, 0f);
        }

        /**
         * Builds the hologram.
         *
         * @return The built hologram data
         */
        HologramData build();
    }
}