# Animations

Animations cycle text frames in text displays. Saved in `animations/<name>.json`. Apply via GUI (planned) or manual reference in text lines.

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

To use: In text display lines, reference animation (future feature; currently manual cycling via updateRate).
