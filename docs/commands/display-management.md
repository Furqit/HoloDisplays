# Display Management

These commands manage standalone displays: creating, listing, deleting, and editing.

## Create Display

```
/holodisplays display create <name> <type> [<args>] [hologram:<hologramName>]
```

* **Description**: Creates a display. Types: text, item, block, entity. Optionally add to hologram.
* **Arguments**:
  * `<name>`: Unique ID (word).
  * `<type>`:
    * `text <content>`: String (supports formatting).
    * `item <itemId>`: Registry ID (suggests).
    * `block <blockId>`: Registry ID (suggests).
    * `entity <entityId>`: Registry ID (suggests).
  * `hologram:<hologramName>`: Optional (suggests; adds to hologram).
*   **Examples**:

    ```
    /hd display create mysign text "Welcome!"
    /holo display create mysword item minecraft:diamond_sword hologram:myholo
    ```
* **Output**: Opens display editor GUI. Feedback: "Display created".

## List Displays

```
/holodisplays display list
```

* **Description**: Opens GUI listing all displays.
*   **Example**:

    ```
    /holodisplays display list
    ```
* **Output**: GUI with edit/delete options.

## Delete Display

```
/holodisplays display delete <name>
```

* **Description**: Prompts confirmation to delete.
* **Arguments**:
  * `<name>`: ID (suggests).
*   **Example**:

    ```
    /hd display delete mysign
    ```
* **Output**: Confirmation GUI; deletes on confirm. Feedback: "Display deleted".

## Edit Display

```
/holodisplays display edit <name> [<property> <value>...]
```

* **Description**: Updates properties or opens editor.
* **Arguments**:
  * `<name>`: ID (suggests).
  * Common:
    * `scale <x> <y> <z>` / `scale reset`.
    * `billboard <mode>` / `billboard reset`.
    * `rotation <pitch> <yaw> <roll>` / `rotation reset`.
    * `condition <placeholder>` / `condition remove`.
  * Text:
    * `text line add <content>`.
    * `text line <index> <content>`.
    * `text line delete <index>`.
    * `text width <value>`.
    * `background <color> <opacity>` / `background reset`.
    * `opacity <value>`.
    * `shadow <true/false>`.
    * `seeThrough <true/false>`.
    * `alignment <mode>`.
  * Item:
    * `item id <itemId>`.
    * `item displayType <type>`.
    * `item customModelData <value>` / `reset`.
  * Block:
    * `block id <blockId>`.
  * Entity:
    * `entity id <entityId>`.
    * `entity glow <true/false>`.
    * `entity pose <pose>` / `reset`.
*   **Examples**:

    ```
    /holo display edit mysign text line add "Line 2"
    /hd display edit mysword item displayType head
    ```
* **Output**: Updates, feedback. No args: GUI.
