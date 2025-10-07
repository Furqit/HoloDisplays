# Hologram Creation and Management

These commands handle holograms: creating, listing, deleting, editing, moving, and managing lines (displays).

## Create Hologram

```
/holodisplays hologram create <name>
```

* **Description**: Creates a new hologram at your position with a default text display.
* **Arguments**:
  * `<name>`: Unique ID (word).
*   **Example**:

    ```
    /hd hologram create myholo
    ```
* **Output**: Opens hologram editor GUI. Feedback: "Hologram 'myholo' created".
* **Note**: Default display is text "Hello, %player:name%"; edit or add others.

## List Holograms

```
/holodisplays hologram list
```

* **Description**: Opens GUI listing all holograms for editing/deleting.
*   **Example**:

    ```
    /holo hologram list
    ```
* **Output**: GUI with hologram names and actions.

## Delete Hologram

```
/holodisplays hologram delete <name>
```

* **Description**: Prompts confirmation to delete a hologram and its file.
* **Arguments**:
  * `<name>`: Hologram ID (suggests existing).
*   **Example**:

    ```
    /holodisplays hologram delete myholo
    ```
* **Output**: Confirmation GUI; on confirm, deletes and returns to main menu. Feedback: "Hologram deleted".

## Edit Hologram

```
/holodisplays hologram edit <name> [<property> <value>...]
```

* **Description**: Opens editor or updates properties (scale, rotation, etc.).
* **Arguments**:
  * `<name>`: Hologram ID (suggests).
  * Properties:
    * `scale <x> <y> <z>`: Scale array \[x,y,z] (floats >=0.1).
    * `scale reset`: Reset to \[1,1,1].
    * `billboard <mode>`: fixed/horizontal/vertical/center.
    * `billboard reset`: Center.
    * `rotation <pitch> <yaw> <roll>`: Degrees (-180 to 180).
    * `rotation reset`: \[0,0,0].
    * `condition <placeholder>`: e.g., "%player:group% == admin".
    * `condition remove`: Remove.
    * `updateRate <ticks>`: Any positive integer.
    * `updateRate reset`: 20.
    * `viewRange <blocks>`: 1-128.
    * `viewRange reset`: 48.
*   **Examples**:

    ```
    /hd hologram edit myholo scale 2 2 2
    /holo hologram edit myholo condition "%player:level% > 5"
    ```
* **Output**: Updates, feedback "Hologram updated". No args: Opens GUI.

## Move Hologram

```
/holodisplays hologram move <name> [<x> <y> <z>]
```

* **Description**: Moves to coordinates (defaults to your position).
* **Arguments**:
  * `<name>`: ID (suggests).
  * `<x> <y> <z>`: Optional doubles.
*   **Example**:

    ```
    /holodisplays hologram move myholo 100 65 200
    ```
* **Output**: Opens editor. Feedback: "Hologram moved".

## Line Commands (Manage Displays in Hologram)

```
/holodisplays hologram line <hologram> <action> [<display> [<x> <y> <z>]]
```

* **Description**: Add/remove/offset displays.
* **Actions**:
  * `add <display>`: Add (suggests displays).
  * `remove <display>`: Remove.
  * `offset <display> <x> <y> <z>`: Set offset array.
*   **Examples**:

    ```
    /hd hologram line myholo add mysign
    /holo hologram line myholo remove mysign
    /holodisplays hologram line myholo offset mysign 0 1 0
    ```
* **Output**: Updates, opens editor. Feedback: "Display added/removed".

See [Display Management](broken-reference) for display commands, Hologram Configurations for formats.
