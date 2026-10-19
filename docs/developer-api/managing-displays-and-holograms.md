# Managing Displays and Holograms

Once created, use these methods to update, remove, or query API-registered content.

## Hologram Management

### Register Hologram

```java
HoloDisplaysAPI api = HoloDisplaysAPI.get("mymod");
boolean success = api.registerHologram("my_hologram", hologramData);
```

* **Description**: Registers a hologram with the given ID and data. The hologram will be managed by HoloDisplays but not saved to the config file. All referenced displays must already exist (either in config or API-registered).
* **Parameters**:
  * `id`: Simple identifier (e.g., "my_hologram").
  * `hologram`: The HologramData to register (use HologramBuilder to create).
* **Returns**: `true` if registration was successful, `false` otherwise (e.g., duplicate or missing displays).
* **Notes**: Triggers visibility updates for online players. See Creating Holograms for building HologramData.

### Update Hologram

```java
boolean success = api.updateHologram("my_hologram", updatedHologramData);
```

* **Description**: Updates properties (e.g., position, displays) of an existing API-registered hologram. Respawns the hologram for viewers if changes require it.
* **Parameters**:
  * `id`: The hologram identifier.
  * `hologram`: The updated HologramData.
* **Returns**: `true` if the hologram existed and was updated, `false` if not found or invalid (e.g., missing displays).
* **Notes**: Validates referenced displays. Use for dynamic changes like position or conditions.

### Unregister Hologram

```java
boolean success = api.unregisterHologram("my_hologram");
```

* **Description**: Stops rendering and removes the hologram from API management. Does not affect config-saved holograms.
* **Parameters**:
  * `id`: The hologram identifier.
* **Returns**: `true` if removed successfully, `false` if not found.
* **Notes**: Removes from all viewers and trackers.

### Check Registration

```java
boolean exists = api.isHologramRegistered("my_hologram");
```

* **Description**: Checks if a hologram with the given ID is registered via the API (does not check config holograms).
* **Parameters**:
  * `id`: The hologram identifier.
* **Returns**: `true` if registered, `false` otherwise.
* **Notes**: Useful for conditional logic before updating or unregistering.

### Unregister All Holograms

```java
int removed = api.unregisterAllHolograms();
```

* **Description**: Unregisters all holograms created by your mod. Useful for cleanup when unloading your mod.
* **Parameters**: None.
* **Returns**: The number of holograms removed.
* **Notes**: Removes from viewers and trackers; does not affect config holograms.

## Display Management

### Create Displays

To create API-registered displays (Text, Item, Block, Entity), use the builder methods. These displays can then be referenced in holograms.

See Creating Displays for details on `createTextDisplay`, `createItemDisplay`, `createBlockDisplay`, and `createEntityDisplay` methods, including builder configuration (e.g., text lines, item/block/entity IDs, scale, rotation, conditions).

### Get Display

```java
DisplayData display = api.getDisplay("my_text");
```

* **Description**: Retrieves an API-registered display by ID (does not check config displays).
* **Parameters**:
  * `id`: The display identifier.
* **Returns**: The DisplayData if found, `null` otherwise.
* **Notes**: Use to fetch for updates or verification before adding to holograms.

### Update Display

```java
boolean success = api.updateDisplay("my_text", updatedDisplayData);
```

* **Description**: Updates an existing API-registered display with new data. Uses efficient metadata updates when possible, only respawning entities when necessary. Automatically updates all holograms that use this display.
* **Parameters**:
  * `id`: The display identifier.
  * `display`: The new DisplayData.
* **Returns**: `true` if update was successful, `false` if display not found or update failed.
* **Notes**: Changes to text content or colors use metadata updates. Changes to item/block IDs, rotation, or scale require respawning.

### Update Display Animation

```java
boolean success = api.updateDisplayAnimation("my_sword", "drop_item");
```

* **Description**: Assigns a transform animation to an existing API-registered item or block display. Metadata update; entities are not respawned.
* **Parameters**:
  * `id`: The display identifier.
  * `animationName`: The animation name, or `null` to revert the display to its static transform.
* **Returns**: `true` if updated, `false` if the display is not API-registered or not an item or block display.
* **Notes**: The animation does not need to exist yet; the display stays static until it is registered.

### Check Display Registration

```java
boolean exists = api.isDisplayRegistered("my_text");
```

* **Description**: Checks if a display with the given ID is registered via the API (does not check config displays).
* **Parameters**:
  * `id`: The display identifier.
* **Returns**: `true` if registered, `false` otherwise.
* **Notes**: Useful for conditional logic before updating or unregistering.

### Unregister Display

```java
boolean success = api.unregisterDisplay("my_text");
```

* **Description**: Unregisters a previously registered display. This will affect all holograms using this display.
* **Parameters**:
  * `id`: The display identifier.
* **Returns**: `true` if unregistration was successful, `false` if display not found.
* **Notes**: All holograms using this display will be respawned to reflect the change.

### Unregister All Displays

```java
int removed = api.unregisterAllDisplays();
```

* **Description**: Unregisters all displays created by your mod. Useful for cleanup when unloading your mod.
* **Parameters**: None.
* **Returns**: The number of displays removed.
* **Notes**: All holograms using these displays will be respawned; does not affect config displays.

## Text Animations

Text animations cycle the frames of an `<animation:name>` placeholder placed anywhere in a display's text. Assigning one is content-driven: put the placeholder in the lines.

### Save Text Animation

```java
boolean success = api.saveTextAnimation("blink", List.of("=|=", "==="), 10);
```

Then use it in a line (through the builder or `updateDisplayText`):

```java
api.updateDisplayText("my_text", List.of("<animation:blink>"));
```

* **Description**: Registers a text animation for runtime use. Runtime animations last until restart; persistent ones live in `config/holodisplays/animations/`.
* **Parameters**:
  * `name`: The animation name.
  * `frames`: The non-empty list of frame texts.
  * `interval`: Ticks each frame is shown (minimum 1).
* **Returns**: `true` if registered (registering the same name again replaces it).

### List Text Animations

```java
Set<String> names = api.getTextAnimationNames();
```

* **Description**: Names of all registered text animations, including file-based ones.

### Delete Text Animation

```java
boolean success = api.deleteTextAnimation("blink");
```

* **Description**: Removes a text animation. File-based ones reappear on the next reload; lines referencing a missing animation show the invalid-animation placeholder.

## Transform Animations

Transform animations drive the scale, rotation and translation of item and block displays. Names accept letters, digits, dots, dashes and underscores.

### Save Transform Animation

```java
boolean success = api.saveTransformAnimation("spin", builder -> {
    builder.interval(126).interpolation("linear").loop(true);
    builder.rotation(0f, 0f, 0f, 0f, 0f, 360f);
});
```

Apply a runtime animation to a display:

```java
api.updateDisplayAnimation("my_sword", "spin");
```

* **Description**: Registers a transform animation for runtime use. Runtime animations last until restart; persistent animations live in `config/holodisplays/animations/`, and the bundled `drop_item` is always available.
* **Parameters**:
  * `name`: The animation name.
  * `builder`: The TransformAnimationBuilder consumer.
* **Returns**: `true` if registered (registering the same name again replaces it).
* **Notes**: Timing: `interval` is the ticks for one from-to sweep. With `loop(true)` a full cycle takes `interval` ticks (from-to restart, for spins); ping-pong takes `2 x interval`. `interpolation` is `"sine"` (default) or `"linear"`.

### List Transform Animations

```java
Set<String> names = api.getTransformAnimationNames();
```

* **Description**: Names of all registered transform animations, including file-based ones and the bundled `drop_item`.

### Delete Transform Animation

```java
boolean success = api.deleteTransformAnimation("spin");
```

* **Description**: Removes a transform animation. File-based ones reappear on the next reload.
