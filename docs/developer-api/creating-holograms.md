# Creating Holograms

Use the fluent HologramBuilder to configure and build `HologramData`, then register it.

## Hologram Builder

```java
HoloDisplaysAPI api = HoloDisplaysAPI.get("mymod");

HologramData hologram = api.createHologramBuilder()
    .world("minecraft:overworld")
    .position(100f, 64f, 200f)
    .scale(1f, 1f, 1f)
    .billboardMode("center")
    .updateRate(20)  // Ticks
    .viewRange(48.0)  // Blocks
    .rotation(0f, 0f, 0f)
    .condition("%server_tps% > 19")
    .addDisplay("my_text", 0f, 0f, 0f)  // Display ID + offset [x, y, z]
    .addDisplay("my_sword", 0f, -0.5f, 0f)
    .build();

// Register for rendering
api.registerHologram("my_hologram", hologram);
```

* **Methods**:
  * `world(String id)`: Dimension (default "minecraft:overworld").
  * `position(float x, y, z)`: Absolute position.
  * `scale(float x, y, z)`: Overall scale (default \[1,1,1]).
  * `billboardMode(String mode)`: fixed/horizontal/vertical/center.
  * `updateRate(int ticks)`: Refresh rate (default 20).
  * `viewRange(double blocks)`: Visibility distance (default 48.0).
  * `rotation(float x, y, z)`: Hologram rotation (default \[0,0,0]).
  * `condition(String placeholder)`: Visibility or null.
  * `addDisplay(String id, float x, y, z)`: Add with offset (or without for \[0,0,0]).
  * `build()`: Returns HologramData.

## HologramData Structure

* `displays`: List (displayId + offset \[x,y,z]).
* `position`: \[x, y, z] array.
* `world`: String.
* `scale`: \[x, y, z] array.
* `billboardMode`: BillboardMode (CENTER default).
* `updateRate`: Int.
* `viewRange`: Double.
* `rotation`: \[x, y, z] array.
* `conditionalPlaceholder`: String?.

Register to make visible; updates respawn displays for viewers.
