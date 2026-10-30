# Display Configurations

Displays are visual elements (text, item, block, entity) saved in `displays/<name>.json`. They are referenced by holograms.

Enum values (`billboardMode`, `alignment`, `pose`) are written uppercase and accepted in any case. All types share:

* **rotation**: Array [x, y, z] (degrees).
* **leftRotation**: Array [x, y, z, w] (quaternion).
* **rightRotation**: Array [x, y, z, w] (quaternion).
* **scale**: Array \[x, y, z] (default \[1,1,1]).
* **billboardMode**: String (FIXED, VERTICAL, HORIZONTAL, CENTER; except entity).
* **conditionalPlaceholder**: String condition on a placeholder value (e.g., "%player:group% = admin"). Operators: `=`, `!=`, `>`, `<`, `>=`, `<=`, `contains`, `!contains`, `startsWith`, `endsWith` (numeric comparison for `>`, `<`, `>=`, `<=`).

## Text Display

For messages with formatting.

### Properties

* **lines**: Array of strings (multi-line; `<gr #ff0000 #00ff00>Gradient</gr>`).
* **lineWidth**: Int (1-200).
* **backgroundColor**: String (hex with alpha, "FFFFFF80").
* **textOpacity**: Int (0-100).
* **shadow**: Bool.
* **seeThrough**: Bool.
* **alignment**: String (LEFT, CENTER, RIGHT).

### Example

```json
{
  "type": "text",
  "lines": ["Hello!", "%player:name%"],
  "lineWidth": 100,
  "backgroundColor": "FFFFFF80",
  "textOpacity": 100,
  "shadow": true,
  "seeThrough": false,
  "alignment": "CENTER",
  "rotation": [0, 0, 0],
  "scale": [1, 1, 1],
  "billboardMode": "CENTER",
  "conditionalPlaceholder": "%server_tps% > 19"
}
```

## Item Display

3D items.

### Properties

* **id**: String (e.g., "minecraft:diamond").
* **itemDisplayType**: String (none, thirdperson_lefthand, etc., default: ground).
* **customModelData**: Int (>=1).
* **animation**: String, name of a transform animation from `animations/<name>.json` (optional).

### Example

```json
{
  "type": "item",
  "id": "minecraft:diamond",
  "itemDisplayType": "gui",
  "customModelData": 1,
  "rotation": [0, 90, 0],
  "scale": [2, 2, 2],
  "billboardMode": "FIXED"
}
```

## Block Display

3D blocks.

### Properties

* **id**: String (e.g., "minecraft:stone").
* **properties**: Map of strings (e.g., `{"facing": "north"}`).
* **animation**: String, name of a transform animation from `animations/<name>.json` (optional).

### Example

```json
{
  "type": "block",
  "id": "minecraft:stone",
  "properties": {
    "facing": "north"
  },
  "rotation": [0, 0, 0],
  "scale": [1, 1, 1],
  "billboardMode": "CENTER"
}
```

## Entity Display

Mobs/entities.

### Properties

* **id**: String (e.g., "minecraft:zombie").
* **glow**: Bool.
* **pose**: String (STANDING, CROUCHING, etc.).

### Example

```json
{
  "type": "entity",
  "id": "minecraft:zombie",
  "glow": true,
  "pose": "STANDING",
  "rotation": [0, 180, 0],
  "scale": [0.5, 0.5, 0.5]
}
```

See Overview for global setup, [Hologram Configurations](../configuration/hologram-configurations.md) for grouping.
