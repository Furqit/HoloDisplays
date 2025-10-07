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
