# Commands

HoloDisplays provides a comprehensive command system for managing holograms and displays. All commands require operator permissions (level 2+). Use `/op <player>` to grant access.

The main command is `/holodisplays` with aliases `/hd` and `/holo`. Running without arguments opens the main GUI menu.

Commands are structured under `display` and `hologram` subcommands. Tab-completion (suggestions) is available for names, IDs, etc. Many commands open GUIs for further editing.

## Permissions and Usage

* **Permission Level**: 2 (operator).
* **GUI Integration**: Commands often open editors (e.g., anvil for names, inventory for properties).
* **Feedback**: Success/errors shown in chat (e.g., "Hologram created").
* **Reload**: Use `/holodisplays reload` after config edits.

See subsections for details:

* [Hologram Creation and Management](broken-reference)
* [Display Management](broken-reference)

Cross-references:

* For hologram formats, see [Hologram Configurations](broken-reference).
* For API alternatives, see [Developer API](broken-reference).

## Quick Examples

* Open GUI: `/hd`
* Create hologram: `/holodisplays hologram create myholo`
* Create text display: `/holo display create mysign text "Hello!"`
* Add display to hologram: `/hd hologram line myholo add mysign`
* Edit scale: `/holodisplays hologram edit myholo scale 2 2 2`
* Reload: `/hd reload`

## Admin Commands

These commands are for server administration: reloading configs and general management.

### Reload

```
/holodisplays reload
```

* **Description**: Reloads all configuration files (holograms, displays, animations) and reinitializes handlers without restarting the server. Respawns holograms for viewers.
* **Permission**: Level 2+.
*   **Example**:

    ```
    /hd reload
    ```
* **Output**: Reloads and updates visibility. Feedback: "Configuration reloaded successfully".
* **Note**: Use after manual config edits. May cause brief flicker in holograms; safe for production but test first.
* **Cross-reference**: See [Configuration](broken-reference) for reload effects.

### Main Command (Admin Access)

```
/holodisplays
/hd
/holo
```

* **Description**: Opens the main GUI menu for admins to manage holograms/displays.
* **Permission**: Level 2+.
*   **Example**:

    ```
    /holo
    ```
* **Output**: GUI with options for creation, listing, etc.
* **Note**: All subcommands inherit this permission. Use for quick access or when commands are preferred over GUI.

For creation, see Hologram Creation and Management and Display Management.
