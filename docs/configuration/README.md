# Configuration

HoloDisplays stores data in JSON files under `config/holodisplays/` (auto-created). The parser is lenient: `//` and `/* */` comments, trailing commas, and unknown fields are all accepted, so hand-editing is forgiving. Edit via the GUI, commands, or manually, then run `/holodisplays reload` to apply changes without a restart.

## Structure

* **holograms/**: `.json` files for each hologram (position, displays, properties).
* **displays/**: `.json` files for display templates (text, item, block, entity).
* **animations/**: `.json` files for animation sequences (text frame swaps and item/block transform animations; the mod ships a `drop_item.json` example).
* **lang/**: `<locale>.json` language files for command feedback and GUI text (see [Translations (i18n)](i18n.md)).

## Global Settings

No global config file; settings are per-hologram/display. Performance tips:

* **viewRange**: Limit to 16-64 blocks to reduce packet load.
* **updateRate**: 20 ticks (1s) default; higher for static content.
* **Scale/Rotation**: Keep reasonable values to avoid rendering issues.

## API Integrations

* **Placeholder API**: Enables dynamic content (e.g., %player:name% or the %server\_tps% > 19 condition) in lines and conditional placeholders. Just install the mod; no config needed.
* **Mod Compatibility**: Works with Fabric mods; for custom displays, extend the API builders.

## Custom JSON Schemas

Config files are self-documenting; unknown fields are ignored, so stick to the core schema for compatibility.

* **Vector Fields**: Use arrays \[x, y, z] for rotation/scale/offset.
* **Registry IDs**: Use full IDs (e.g., "minecraft:stone"); tab-complete in commands.
* **Comments**: `//` and `/* */` comments are skipped while loading.

## Validation and Troubleshooting

* **Load Errors**: Server log reports JSON parse issues and missing referenced displays.
* **Hologram Not Visible**: Verify position/world, viewRange, conditions, and reload.
* **Performance Issues**: Reduce updateRate/viewRange for many holograms; monitor TPS.
* **Backup**: Copy `config/holodisplays/` before big changes; data auto-saves.
* **Reload Side Effects**: Reloading respawns holograms, so they may briefly hide; use in low-traffic times.
