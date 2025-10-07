# Developer API

HoloDisplays provides a public Java API for other mods to create and manage holograms and displays programmatically. The API is accessible via [`HoloDisplaysAPI`](https://github.com/Furq07/HoloDisplays/blob/main/src/main/java/dev/furq/holodisplays/api/HoloDisplaysAPI.java). All API-registered content is managed in-memory (not saved to config files) and cleared on reload/shutdown.

## Getting Started

### Add Dependency

**1. Download HoloDisplays JAR**

Download the latest release from [Modrinth](https://modrinth.com/mod/holodisplays) and place it in your project's `libs/` folder.

**2. Add to Gradle**

Since HoloDisplays is not published to a Maven repository, use a file-based dependency in your `build.gradle` or `build.gradle.kts`:

```groovy
// build.gradle
dependencies {
    modImplementation files('libs/holodisplays-<version>+<mc-version>.jar')
}
```

Or for Kotlin DSL:

```kotlin
// build.gradle.kts
dependencies {
    modImplementation(files("libs/holodisplays-<version>+<mc-version>.jar"))
}
```

> **Important**: Use `modImplementation` (not `include`). HoloDisplays must be installed separately by users - do not bundle it with your mod.

**3. Add to fabric.mod.json**

```json
"depends": {
  "holodisplays": "*"
}
```

### Use the API

Access the API with your mod ID:

```java
import dev.furq.holodisplays.api.HoloDisplaysAPI;

HoloDisplaysAPI api = HoloDisplaysAPI.get("mymod");
```

**Important Rules**:

* Provide your mod ID when getting the API instance.
* Use simple IDs like `"my_hologram"` - your mod ID is prepended automatically.
* Do not use `"minecraft"` as your mod ID.
* Displays referenced in holograms must exist (via config or API registration).
* Use `api.unregisterAllHolograms()` when unloading your mod.

## API Overview

The API supports:

* Creating displays (text, item, block, entity) with builders.
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
