# Creating Displays

Displays are created using builder consumers. Each type has a dedicated builder. Registered displays are available for holograms.

## Text Display

```java
HoloDisplaysAPI api = HoloDisplaysAPI.get("mymod");
DisplayData textDisplay = api.createTextDisplay("my_text", builder -> {
    builder.text("Hello", "World!");  // Multi-line
    builder.scale(1.5f, 1.5f, 1.5f);
    builder.rotation(0f, 45f, 0f);
    builder.billboardMode("center");
    builder.condition("%player:group% == vip");
    builder.backgroundColor("FFFFFF", 50);  // White, 50% opacity
    builder.shadow(true);
    builder.seeThrough(true);
    builder.opacity(0.8f);  // 80% opacity
});
```

* **Methods**:
  * `text(String... lines)`: Set lines (formatting supported).
  * `scale(float x, y, z)`: Scale.
  * `rotation(float x, y, z)`: Rotation.
  * `leftRotation(float x, y, z, w)`: Left rotation (quaternion).
  * `rightRotation(float x, y, z, w)`: Right rotation (quaternion).
  * `billboardMode(String mode)`: fixed/horizontal/vertical/center.
  * `condition(String placeholder)`: Or null.
  * `backgroundColor(String hex, int opacity)`: Hex (6 chars), opacity 0-100.
  * `shadow(boolean)`: Shadow.
  * `seeThrough(boolean)`: Transparent BG.
  * `opacity(float 0-1)`: Text alpha.

## Item Display

```java
DisplayData itemDisplay = api.createItemDisplay("my_sword", builder -> {
    builder.item("minecraft:diamond_sword");
    builder.scale(2f, 2f, 2f);
    builder.rotation(0f, 90f, 0f);
    builder.billboardMode("fixed");
    builder.condition(null);
});
```

* **Methods**: `item(String id)`, plus common (scale, rotation, leftRotation, rightRotation, billboard, condition).

## Block Display

```java
DisplayData blockDisplay = api.createBlockDisplay("my_stone", builder -> {
    builder.block("minecraft:stone");
    builder.properties(Map.of("faced", "north"));
    builder.scale(1f, 1f, 1f);
    builder.rotation(0f, 0f, 0f);
});
```

* **Methods**: `block(String id)`, `properties(Map<String, String>)`, plus common.

## Entity Display

```java
DisplayData entityDisplay = api.createEntityDisplay("my_zombie", builder -> {
    builder.entity("minecraft:zombie");
    builder.glow(true);
    builder.pose("standing");
    builder.scale(1f, 1f, 1f);
    builder.rotation(0f, 180f, 0f);
    builder.condition(null);
});
```

* **Methods**:
  * `entity(String id)`: Entity registry ID (e.g., "minecraft:zombie").
  * `glow(boolean)`: Glowing outline effect.
  * `pose(String)`: Entity pose (e.g., "standing", "crouching", "sneaking").
  * Plus common (scale, rotation, leftRotation, rightRotation, condition).
* **Notes**: Entity displays do not support billboardMode.

## DisplayData

Returns `DisplayData` wrapper for BaseDisplay subtypes. Use in holograms via ID.

All builders throw IllegalArgumentException for invalid values (e.g., bad hex, out-of-range opacity).
