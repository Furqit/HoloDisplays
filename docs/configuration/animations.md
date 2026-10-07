# Animations

Animations cycle text frames inside text displays. Saved in `animations/<name>.json`. Reference them in text lines with `<animation:name>`.

## Format

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

## Properties

* **frames**: List of strings (each frame's text, supports formatting).
* **interval**: Int (ticks; lower for faster animation).

## Example

`animations/loading.json`:

```json
{
  "frames": ["Frame 1", "Frame 2"],
  "interval": 40
}
```

To use: reference it in a text display line, e.g. `"Loading <animation:loading>"`. Interval must be at least 1 (values below 1 are treated as 1).
