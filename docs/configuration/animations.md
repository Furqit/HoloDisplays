# Animations

Animations live in `animations/<name>.json` (apply with `/holodisplays reload`). Two kinds share the folder: text frame animations, referenced in text lines with `<animation:name>`, and transform animations for item/block displays. A file with `frames` is treated as a text animation, any other shape as a transform animation.

## Text Animations

Text animations cycle text frames inside text displays.

### Format

```json
{
  "frames": [
    // Array of text frames
    "Loading.",
    "Loading..",
    "Loading..."
  ],
  "interval": 10 // Ticks between frames (default 20)
}
```

### Properties

* **frames**: List of strings (each frame's text, supports formatting).
* **interval**: Int (ticks; lower for faster animation).

### Example

`animations/loading.json`:

```json
{
  "frames": ["Frame 1", "Frame 2"],
  "interval": 40
}
```

Reference it in a text display line, e.g. `"Loading <animation:loading>"`. Interval must be at least 1 (values below 1 are treated as 1).

## Transform Animations

Item and block displays can be animated with transform animations: scale, rotation and translation eased between two values.

### Format

```json
{
  "interval": 60,
  "interpolation": "sine",   // "linear" or "sine" (default "sine")
  "loop": false,             // false = ping-pong (from to to and back), true = from to to, then restart
  "scale":       { "from": 0.9, "to": 1.1 },
  "rotation":    { "from": [0, 0, 0], "to": [0, 360, 0] },
  "translation": { "from": [0, 0, 0], "to": [0, 0.15, 0] }
}
```

### Properties

* **scale**: Uniform scale multiplier, animated from `from` to `to`.
* **rotation**: Relative rotation in degrees (XYZ), added on top of the display's static rotation.
* **translation**: Relative offset, added on top of the display's computed translation (block centering included).
* **interval**: Ticks for one `from` to `to` sweep (default 20, minimum 1). With `loop` a full cycle takes `interval` ticks; ping-pong takes `2 x interval`.
* **interpolation**: `linear` (constant speed) or `sine` (eases in and out; default).
* **loop**: `false` (default) smoothly ping-pongs between `from` and `to`; `true` jumps back to `from` after reaching `to`, which suits continuous spinning (e.g. rotation `0` to `360`).
* Per-property overrides: `scale`, `rotation` and `translation` each accept their own `interval`, `interpolation` and `loop`, falling back to the top-level values when omitted.

Values are relative to the display's configured base transform: animated scale multiplies the base scale, rotation and translation are deltas added to the base. Transform updates are interpolated by the client at full framerate, so motion renders fluidly at any interval.

### Examples

`animations/bob.json` (slow float):

```json
{
  "interval": 60,
  "interpolation": "sine",
  "translation": { "to": [0, 0.15, 0] }
}
```

`animations/spin.json`:

```json
{
  "interval": 120,
  "interpolation": "linear",
  "loop": true,
  "rotation": { "to": [0, 360, 0] }
}
```

Assign one to a display with `/holodisplays display edit <name> animation <file>` (or `animation reset` to clear), or set `"animation": "bob"` directly on an item/block display config file. Animated values are recalculated every tick while players are viewing the hologram.

## Built-in Animations

The mod ships the following animations, written into `config/holodisplays/animations/` at startup when the file does not exist yet. Edit or delete them freely; they are only recreated if missing at the next start.

* **drop_item**: Replicates the vanilla dropped item: one spin every ~6.3s (`linear`, `loop`) with a ~3.1s bob (`sine` ping-pong).
