# Getting Started

## Installation

1. Ensure Fabric Loader and Fabric API are installed for your Minecraft version (1.20.5+).
2. Download HoloDisplays from [Modrinth](https://modrinth.com/mod/holodisplays) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/holodisplays).
3. Place the `.jar` file in your `mods` folder.
4. Launch Minecraft and verify the mod loads (check the mods list in the game menu).
5. Optional: Install Placeholder API for dynamic placeholders (e.g., %player:name%).

## Permissions

Commands and GUI access require operator permissions (level 2+). Use `/op <player>` in-game or server console.

## Basic Usage

1. Run `/holodisplays` (or aliases `/hd`, `/holo`) to open the main GUI menu.
2. From the GUI, create holograms and add displays.
3. Use commands for quick actions, e.g., `/holodisplays hologram create myholo` to make a new hologram.
4. Holograms are saved automatically to `config/holodisplays/` and load on server start.

## Key Concepts

* **Displays**: Individual visual elements.
  * Text: Multi-line strings with formatting (e.g., `<gr #ffffff #008000>Hello</gr>` for gradients).
  * Item/Block/Entity: 3D models with properties like scale and rotation.
  * Properties: Rotation, scale, billboard mode (how it faces the player), conditions. See Display Types for details.
* **Holograms**: Collections of displays positioned relative to a central point.
  * Each display has an offset (x, y, z) from the hologram's position.
  * Hologram-wide properties: Update rate (ticks between refreshes), view range (distance to see it), conditions.
* **Animations**: Sequences of text frames that cycle at set intervals. Stored separately and applied to text displays. See configuration for format.
* **Conditions**: Use placeholders like `%player:group% == admin` to show/hide based on player data (requires Placeholder API).

## GUI Navigation

The GUI is the primary way to manage content. Access via `/holodisplays`.

### Main Menu

* **Manage Holograms**: List, create, edit, delete holograms.
* **Manage Displays**: List, create, edit, delete standalone displays.
* **Reload**: Refresh configs without restarting.

### Hologram Editor

* View/edit properties (position, scale, etc.).
* Add/remove displays with offsets.
* Save changes apply immediately.

### Display Editors

* **Text**: Edit lines, width, background, opacity, shadow, alignment.
* **Item**: Set item ID, display type, custom model data.
* **Block**: Set block ID.
* **Entity**: Set entity ID, glow, pose.

For a hands-on tutorial, see Creating your first hologram.

## Command Overview

Commands provide an alternative to the GUI. See Creating your first hologram for examples. Full list:

* `/holodisplays [display|hologram] create/list/delete/edit/move/line`
* `/holodisplays reload`

Configuration files are auto-generated but editable; see Display Types for formats.
