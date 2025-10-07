# Creating your first hologram

This guide walks you through creating a simple hologram using both the GUI and commands. We'll make a welcome hologram with a text display.

## Prerequisites

* Operator permissions.
* Server running with HoloDisplays installed.
* Optional: Placeholder API for dynamic text.

## Step 1: Open the Main Menu

Run the main command to access the GUI:

```
/holodisplays
```

This opens the main menu with options for holograms and displays.

Alternatively, use aliases:

```
/hd
/holo
```

## Step 2: Create a Display

Displays are the building blocks. Let's create a text display.

### Using GUI

1. In the main menu, click "Manage Displays".
2. Click "Create Display".
3. Select "Text Display".
4. Enter name: `welcome_text`.
5. Enter content: `Welcome to the server!` (or use anvil input for multi-line).
6. Click "Create".
7. The display editor opens: Adjust properties like alignment (center), shadow (true), then save.

### Using Commands

```
/holodisplays display create welcome_text text "Welcome to the server!"
```

* This creates the display and opens the edit GUI for further tweaks (e.g., `/hd display edit welcome_text alignment center`).

Feedback: "Display 'welcome\_text' created".

## Step 3: Create the Hologram

Holograms group displays.

### Using GUI

1. Back to main menu, click "Manage Holograms".
2. Click "Create Hologram".
3. Enter name: `welcome_holo`.
4. The hologram is created at your position with a default display; editor opens.
5. In editor, remove default if needed, add "welcome\_text" via "Manage Displays".
6. Set offset to \[0, 0, 0] for center.
7. Adjust hologram properties: scale \[1,1,1], viewRange 32, then save.

### Using Commands

```
/holodisplays hologram create welcome_holo
```

* Creates at your position with default text display, opens editor.

Add the custom display:

```
/hd hologram line welcome_holo add welcome_text
```

* Opens hologram editor; set offset if needed: `/holo hologram line welcome_holo offset welcome_text 0 0 0`.

Edit properties:

```
/holodisplays hologram edit welcome_holo viewRange 32
```

Feedback: "Hologram 'welcome\_holo' created".

## Step 4: Position and Test

### Move the Hologram

```
/holodisplays hologram move welcome_holo 100 65 200
```

* Moves to coordinates; opens editor.

Or use GUI: In hologram editor, adjust position.

### Test

* Walk away and back within view range (32 blocks); the hologram should appear.
* If using placeholders, test with different players.
* Reload for changes: `/hd reload`.

## Step 5: Advanced Tweaks

* Add condition: `/holodisplays hologram edit welcome_holo condition "%player:group% == default"`
* Add another display: Create an item display (`/hd display create welcome_item item minecraft:grass_block`), add to hologram (`/holo hologram line welcome_holo add welcome_item`), offset \[0, -0.5, 0] for below text.
* Edit via GUI for visual preview.

## Troubleshooting

* Hologram not visible? Check viewRange, position, conditions, reload.
* "Display not found"? Ensure display exists in `displays/`.
* Errors? Check server log for JSON validation.
