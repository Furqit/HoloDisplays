# Creating Displays

Displays are created using builder consumers. Each type has a dedicated builder. Registered displays are available for holograms.

## Text Display

```java
DisplayData textDisplay = api.createTextDisplay("mymod:mytext", builder -> {
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
  * `billboardMode(String mode)`: fixed/horizontal/vertical/center.
  * `condition(String placeholder)`: Or null.
  * `backgroundColor(String hex, int opacity)`: Hex (6 chars), opacity 1-100.
  * `shadow(boolean)`: Shadow.
  * `seeThrough(boolean)`: Transparent BG.
  * `opacity(float 0-1)`: Text alpha.

## Item Display

```java
DisplayData itemDisplay = api.createItemDisplay("mymod:mysword", builder -> {
    builder.item("minecraft:diamond_sword");
    builder.scale(2f, 2f, 2f);
    builder.rotation(0f, 90f, 0f);
    builder.billboardMode("fixed");
    builder.condition(null);
});
```

* **Methods**: `item(String id)`, plus common (scale, rotation, billboard, condition).

## Block Display

```java
DisplayData blockDisplay = api.createBlockDisplay("mymod:mystone", builder -> {
    builder.block("minecraft:stone");
    builder.scale(1f, 1f, 1f);
    builder.rotation(0f, 0f, 0f);
});
```

* **Methods**: `block(String id)`, plus common.

## DisplayData

Returns `DisplayData` wrapper for BaseDisplay subtypes. Use in holograms via ID.

All builders throw IllegalArgumentException for invalid values (e.g., bad hex, out-of-range opacity).
