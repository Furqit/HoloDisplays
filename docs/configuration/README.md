# Configuration

HoloDisplays uses JSON5 files in `config/holodisplays/` for data persistence. Edit via GUI/commands or manually, then `/holodisplays reload` to apply.

## Structure

* **holograms/**: Hologram files.
* **displays/**: Display templates.
* **animations/**: Animation sequences.

No global config; properties are per-object. See subsections for details.

HoloDisplays stores data in JSON5 files under `config/holodisplays/` (auto-created). Use GUI/commands for editing, or manual for advanced tweaks. Reload with `/holodisplays reload` to apply changes without restart.

## Directory Structure

* **holograms/**: `.json` files for each hologram (position, displays, properties).
* **displays/**: `.json` files for display templates (text, item, block, entity).
* **animations/**: `.json` files for text animation sequences.

## Global Settings

No single global config file; settings are per-hologram/display. Performance tips:

* **viewRange**: Limit to 16-64 blocks to reduce packet load.
* **updateRate**: 20 ticks (1s) default; higher for static content.
* **Scale/Rotation**: Keep reasonable to avoid rendering issues.

## Enabling Features

* **Placeholder API**: Install for %player:name% etc. in conditions/lines.
* **Persistence**: All data auto-saves; backups recommended.
* **Reload**: Respawns holograms; use in production carefully.

## API Integrations

* **Placeholder API**: Enable dynamic content (e.g., %player:health%). Install the mod; no config needed.
* **Custom Conditions**: Use in conditionalPlaceholder for visibility (e.g., "%server\_tps% > 19").
* **Mod Compatibility**: Works with Fabric mods; for custom displays, extend API builders.

## Custom JSON Schemas

JSON5 files are self-documenting. Validate with tools like JSON5 validator. Custom fields ignored; stick to core for compatibility.

* **Vector Fields**: Use arrays \[x, y, z] for rotation/scale/offset.
* **Registry IDs**: Use full IDs (e.g., "minecraft:stone"); tab-complete in commands.
* **Comments**: JSON5 allows // comments for notes.

## Validation and Troubleshooting

* Invalid JSON: Server log errors on load.
* Missing references: Hologram won't load if display ID invalid.
* Custom schemas: JSON5 supports comments; stick to formats in subs.
* **Load Errors**: Check server log for JSON parse issues or missing displays (e.g., hologram references non-existent ID).
* **Hologram Not Visible**: Verify position/world, viewRange, conditions, reload.
* **Performance Issues**: Reduce updateRate/viewRange for many holograms; monitor TPS.
* **Backup**: Copy `config/holodisplays/` before edits.
* **Reload Side Effects**: May briefly hide holograms; use in low-traffic times.
