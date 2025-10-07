# Developer API

HoloDisplays provides a public Java API for other mods to create and manage holograms and displays programmatically. The API is accessible via [`HoloDisplaysAPI`](https://github.com/Furq07/HoloDisplays/blob/main/src/main/java/dev/furq/holodisplays/api/HoloDisplaysAPI.java). All API-registered content is managed in-memory (not saved to config files) and cleared on reload/shutdown.

## Getting Started

Add HoloDisplays as a dependency in your `fabric.mod.json`:

```json
"depends": {
  "holodisplays": "*"
}
```

Access the API singleton:

```java
import dev.furq.holodisplays.api.HoloDisplaysAPI;

HoloDisplaysAPI api = HoloDisplaysAPI.get();
```

**Important Rules**:

* IDs must use namespaces (e.g., `mymod:myhologram`). Do not use `minecraft:`.
* Displays referenced in holograms must exist (via config or API registration).
* Call `api.clearAll()` in your mod's shutdown event for cleanup.
* Use `api.unregisterAllHolograms("mymod")` when unloading your mod.

## API Overview

The API supports:

* Creating displays (text, item, block) with builders.
* Building and registering holograms.
* Managing (update, unregister, query) holograms and displays.

For details on data structures, see the Kotlin sources: `HologramData.kt`, `DisplayData.kt`.

See subsections for specific topics:

* Creating Displays
* Creating Holograms
* Managing Displays and Holograms

## Limitations & Notes

* Animations: Not yet API-exposed (planned).
* Validation: API validates IDs and references; errors print to console.
* Performance: Limit displays per hologram; use viewRange wisely.
* Compatibility: Requires Fabric 1.20.5+, Placeholder API optional for conditions.

Source: [GitHub](https://github.com/Furq07/HoloDisplays).
