# Hologram Configurations

Holograms group displays and are saved in `holograms/<name>.json`. They define position, properties, and display list.

## Format

```json5
{
  displays: [  // Array of display lines
    {
      name: "display_id",  // Display from displays/ or API
      offset: [0.0, 0.0, 0.0]  // Relative position [x, y, z]
    }
  ],
  position: {
    world: "minecraft:overworld",  // Dimension ID
    x: 100.5,
    y: 64.0,
    z: 200.0
  },
  rotation: [0.0, 0.0, 0.0],  // Hologram rotation
  scale: [1.0, 1.0, 1.0],  // Overall scale
  billboardMode: "center",  // fixed, horizontal, vertical, center
  updateRate: 20,  // Ticks
  viewRange: 48.0,  // Blocks (1-128)
  conditionalPlaceholder: "%player:group% == admin"  // Optional
}
```

## Properties

* **displays**: List of objects with `name` (display ID) and `offset` \[x,y,z].
* **position**: Object with `world` (string), `x/y/z` (floats).
* **rotation/scale**: Arrays \[x, y, z].
* **billboardMode**: String (applies to displays).
* **updateRate**: Int (lower = more frequent updates).
* **viewRange**: Double (visibility distance).
* **conditionalPlaceholder**: String or omitted (always visible).

## Integration with PlaceholderAPI

Use placeholders in display lines or conditions (e.g., `%player:health%`). Holograms refresh based on updateRate.

## Example

`holograms/welcome.json`:

```json5
{
  displays: [
    {
      name: "welcome_text",
      offset: [0, 0, 0]
    },
    {
      name: "welcome_item",
      offset: [0, -0.5, 0]
    }
  ],
  position: {
    world: "minecraft:overworld",
    x: 0,
    y: 100,
    z: 0
  },
  scale: [1, 1, 1],
  billboardMode: "center",
  updateRate: 20,
  viewRange: 32,
  conditionalPlaceholder: null
}
```

See Overview for global setup, [Display Configurations](broken-reference) for referenced displays.
