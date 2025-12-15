# Display Types

HoloDisplays supports four types of displays: Text, Item, Block, and Entity. Each type has unique properties and use cases. Displays can be created via GUI or commands and saved as JSON files in `config/holodisplays/displays/`.

All display types share common properties:

* **rotation**: Array \[x, y, z] (Euler angles in degrees).
* **scale**: Array \[x, y, z] (multipliers; default \[1, 1, 1]).
* **billboardMode**: String (fixed, horizontal, vertical, center; except entity).
* **conditionalPlaceholder**: String (visibility condition, e.g., "%player:group% == admin").

## Text Display

Ideal for messages, signs, or dynamic info with placeholders.

### Properties

* **lines**: Array of strings (multi-line; supports formatting like `<gr #ff0000 #00ff00>Gradient</gr>`).
* **lineWidth**: Integer (1-200; max characters per line; default auto).
* **backgroundColor**: String (hex with alpha, e.g., "FFFFFF80" for 50% white).
* **textOpacity**: Integer (1-100; default 100).
* **shadow**: Boolean (text shadow; default false).
* **seeThrough**: Boolean (transparent background; default false).
* **alignment**: String (left, right, center; default left).

### JSON Format

```json
{
  "type": "text",
  "lines": ["Hello World!", "%player:name%"],
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

### Example Usage

Create via command: `/holodisplays display create mysign text "Welcome!"` Edit in GUI: Add lines, adjust alignment for centering.

## Item Display

Displays 3D items with customizable render modes (e.g., as held, on ground).

### Properties

* **id**: String (item registry ID, e.g., "minecraft:diamond_sword").
* **displayType**: String (none, thirdperson_lefthand, thirdperson_righthand, firstperson_lefthand, firstperson_righthand, head, gui, ground, fixed).
* **customModelData**: Integer (>=1; optional for custom models).

### JSON Format

```json
{
  "type": "item",
  "id": "minecraft:diamond",
  "displayType": "gui",
  "customModelData": 1,
  "rotation": [0, 90, 0],
  "scale": [2, 2, 2],
  "billboardMode": "fixed",
  "conditionalPlaceholder": null
}
```

### Example Usage

Command: `/hd display create mysword item minecraft:diamond_sword` GUI: Set displayType to "head" for helmet-like view.

## Block Display

Shows 3D blocks, useful for signs or decorations.

### Properties

* **id**: String (block registry ID, e.g., "minecraft:stone").

### JSON Format

```json
{
  "type": "block",
  "id": "minecraft:stone",
  "rotation": [0, 0, 0],
  "scale": [1, 1, 1],
  "billboardMode": "center",
  "conditionalPlaceholder": "%player:level% >= 10"
}
```

### Example Usage

Command: `/holodisplays display create mystone block minecraft:stone` Scale up for large block displays.

## Entity Display

Renders mobs or entities with poses and glow.

### Properties

* **id**: String (entity registry ID, e.g., "minecraft:zombie").
* **glow**: Boolean (glowing outline).
* **pose**: String (STANDING, SNEAKING, etc.; uppercase EntityPose).

### JSON Format

```json5
{
  type: "entity",
  id: "minecraft:zombie",
  glow: true,
  pose: "STANDING",
  rotation: [0, 180, 0],
  scale: [0.5, 0.5, 0.5],
  conditionalPlaceholder: "%player:health% < 10",
}
```

### Example Usage

Command: `/holo display create myzombie entity minecraft:zombie` GUI: Enable glow for highlighted entities.
