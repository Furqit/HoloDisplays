# Managing Displays and Holograms

Once created, use these methods to update, remove, or query API-registered content.

## Hologram Management

### Register Hologram

```java
boolean success = HoloDisplaysAPI.get().registerHologram("mymod:myhologram", hologramData);
```

* **Description**: Registers a hologram with the given ID and data. The hologram will be managed by HoloDisplays but not saved to the config file. The ID must have a namespace other than "minecraft" (e.g., "mymod:my\_hologram"). All referenced displays must already exist (either in config or API-registered).
* **Parameters**:
  * `id`: Unique identifier (namespace:identifier format).
  * `hologram`: The HologramData to register (use HologramBuilder to create).
* **Returns**: `true` if registration was successful, `false` otherwise (e.g., invalid ID, duplicate, or missing displays).
* **Notes**: Triggers visibility updates for online players. See Creating Holograms for building HologramData.

### Update Hologram

```java
boolean success = HoloDisplaysAPI.get().updateHologram("mymod:myhologram", updatedHologramData);
```

* **Description**: Updates properties (e.g., position, displays) of an existing API-registered hologram. Respawns the hologram for viewers if changes require it.
* **Parameters**:
  * `id`: The unique identifier of the hologram.
  * `hologram`: The updated HologramData.
* **Returns**: `true` if the hologram existed and was updated, `false` if not found or invalid (e.g., missing displays).
* **Notes**: Validates referenced displays. Use for dynamic changes like position or conditions.

### Unregister Hologram

```java
boolean success = HoloDisplaysAPI.get().unregisterHologram("mymod:myhologram");
```

* **Description**: Stops rendering and removes the hologram from API management. Does not affect config-saved holograms.
* **Parameters**:
  * `id`: The unique identifier of the hologram.
* **Returns**: `true` if removed successfully, `false` if not found.
* **Notes**: Removes from all viewers and trackers.

### Check Registration

```java
boolean exists = HoloDisplaysAPI.get().isHologramRegistered("mymod:myhologram");
```

* **Description**: Checks if a hologram with the given ID is registered via the API (does not check config holograms).
* **Parameters**:
  * `id`: The unique identifier to check.
* **Returns**: `true` if registered, `false` otherwise.
* **Notes**: Useful for conditional logic before updating or unregistering.

### Unregister All Holograms

```java
int removed = HoloDisplaysAPI.get().unregisterAllHolograms("mymod");
```

* **Description**: Unregisters all holograms with the specified namespace. Useful for cleanup when unloading your mod.
* **Parameters**:
  * `namespace`: The namespace (e.g., "mymod"); cannot be "minecraft".
* **Returns**: The number of holograms removed.
* **Notes**: Removes from viewers and trackers; does not affect config holograms.

## Display Management

### Create Displays

To create API-registered displays (Text, Item, Block), use the builder methods. These displays can then be referenced in holograms.

See Creating Displays for details on `createTextDisplay`, `createItemDisplay`, and `createBlockDisplay` methods, including builder configuration (e.g., text lines, item/block IDs, scale, rotation, conditions).

### Get Display

```java
DisplayData display = HoloDisplaysAPI.get().getDisplay("mymod:mytext");
```

* **Description**: Retrieves an API-registered display by ID (does not check config displays).
* **Parameters**:
  * `id`: The unique identifier of the display.
* **Returns**: The DisplayData if found, `null` otherwise.
* **Notes**: Use to fetch for updates or verification before adding to holograms.
