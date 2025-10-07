# Display Configurations

Displays are visual elements (text, item, block, entity) saved in `displays/<name>.json`. They are referenced by holograms. All types share:

* **rotation**: Array \[x, y, z] (degrees).
* **scale**: Array \[x, y, z] (default \[1,1,1]).
* **billboardMode**: String (fixed, horizontal, vertical, center; except entity).
* **conditionalPlaceholder**: String (e.g., "%player:group% == admin").

## Text Display

For messages with formatting.

### Properties

* **lines**: Array of strings (multi-line; `<gr #ff0000 #00ff00>Gradient</gr>`).
* **lineWidth**: Int (1-200).
* **backgroundColor**: String (hex with alpha, "FFFFFF80").
* **textOpacity**: Int (1-100).
* **shadow**: Bool.
* **seeThrough**: Bool.
* **alignment**: String (left, right, center).

### Example

```json5
{
  "type": "text",
  "lines": ["Hello!", "%player:name%"],
  "lineWidth": 100,
  "backgroundColor": "FFFFFF80",
  "textOpacity": 100,
  "shadow": true,
  "seeThrough": false,
  "alignment": "center",
  "rotation": [0, 0, 0],
  "scale": [1, 1, 1],
  "billboardMode": "center",
  "conditionalPlaceholder": "%server_tps% > 19"
}
```

## Item Display

3D items.

### Properties

* **id**: String (e.g., "minecraft:diamond").
* **displayType**: String (none, thirdperson\_lefthand, etc.).
* **customModelData**: Int (>=1).

### Example

```json5
{
  "type": "item",
  "id": "minecraft:diamond",
  "displayType": "gui",
  "customModelData": 1,
  "rotation": [0, 90, 0],
  "scale": [2, 2, 2],
  "billboardMode": "fixed"
}
```

## Block Display

3D blocks.

### Properties

* **id**: String (e.g., "minecraft:stone").

### Example

```json5
{
  "type": "block",
  "id": "minecraft:stone",
  "rotation": [0, 0, 0],
  "scale": [1, 1, 1],
  "billboardMode": "center"
}
```

## Entity Display

Mobs/entities.

### Properties

* **id**: String (e.g., "minecraft:zombie").
* **glow**: Bool.
* **pose**: String (STANDING, SNEAKING).

### Example

```json5
{
  "type": "entity",
  "id": "minecraft:zombie",
  "glow": true,
  "pose": "STANDING",
  "rotation": [0, 180, 0],
  "scale": [0.5, 0.5, 0.5]
}
```

See Overview for global setup, [Hologram Configurations](broken-reference) for grouping.
