package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import org.joml.Vector3f

object DisplayEdit {
    private val billboardModes = listOf("HORIZONTAL", "VERTICAL", "CENTER", "FIXED")

    fun open(player: ServerPlayer, name: String, returnCallback: () -> Unit = { DisplayList.open(player) }) {
        val display = DisplayConfig.getDisplay(name) ?: return
        val isEntity = display.type is EntityDisplay
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = Lang.get(player, "gui.display_edit.title", "Edit Display"),
            size = 45,
            borderSlots = listOf(11, 13, 15, 30, 32, 36)
        )

        gui.apply {
            val currentScaleValue = if (isEntity) {
                "${display.type.scale?.x ?: 1.0}"
            } else {
                "${display.type.scale?.x ?: 1.0}, ${display.type.scale?.y ?: 1.0}, ${display.type.scale?.z ?: 1.0}"
            }
            setSlot(11, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.display_edit.scale", "Scale"),
                item = Items.SCAFFOLDING,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to currentScaleValue,
                    Lang.get(player, "gui.common.click_change", "Click to change"),
                    Lang.get(player, "gui.display_edit.right_click_reset", "Right click to reset")
                )
            )) { _, type, _, _ ->
                when {
                    type.isRightClick() -> {
                        DisplayManager.updateScale(name, null, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    }

                    else -> editScale(player, name, display.type.scale, isEntity, returnCallback)
                }
            }

            if (!isEntity) {
                setSlot(13, GuiUtils.createGuiItem(
                    name = Lang.get(player, "gui.display_edit.billboard_mode", "Billboard Mode"),
                    item = Items.COMPASS,
                    lore = buildList {
                        addAll(GuiUtils.createCurrentValueLore(Lang.get(player, "gui.common.current", "Current"), display.type.billboardMode?.name?.uppercase()
                            ?: "NONE"))
                        addAll(GuiUtils.createActionLore(
                            Lang.get(player, "gui.common.click_cycle", "Click to cycle through modes"),
                            Lang.get(player, "gui.display_edit.right_click_reset", "Right click to reset")
                        ))
                        addAll(GuiUtils.createLore("Available modes: HORIZONTAL, VERTICAL, CENTER, FIXED"))
                    }
                )) { _, type, _, _ ->
                    when {
                        type.isRightClick() -> {
                            DisplayManager.updateBillboard(name, null, player.createCommandSourceStack())
                            open(player, name, returnCallback)
                        }

                        else -> {
                            val currentMode = display.type.billboardMode?.name ?: "FIXED"
                            val currentIndex = billboardModes.indexOf(currentMode)
                            val nextMode = billboardModes[(currentIndex + 1) % billboardModes.size]
                            DisplayManager.updateBillboard(name, nextMode.lowercase(), player.createCommandSourceStack())
                            open(player, name, returnCallback)
                        }
                    }
                }
            }

            val currentRotationValue = if (isEntity) {
                "${display.type.rotation?.x ?: 0}°, ${display.type.rotation?.y ?: 0}°"
            } else {
                "${display.type.rotation?.x ?: 0}°, ${display.type.rotation?.y ?: 0}°, ${display.type.rotation?.z ?: 0}°"
            }
            setSlot(15, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.display_edit.rotation", "Rotation"),
                item = Items.CLOCK,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to currentRotationValue,
                    Lang.get(player, "gui.common.click_change", "Click to change"),
                    Lang.get(player, "gui.display_edit.right_click_reset", "Right click to reset")
                )
            )) { _, type, _, _ ->
                when {
                    type.isRightClick() -> {
                        DisplayManager.updateRotation(name, null, null, null, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    }

                    else -> editRotation(player, name, display.type.rotation, isEntity, returnCallback)
                }
            }

            setSlot(30, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.display_edit.condition", "Condition"),
                item = Items.REPEATER,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to (display.type.conditionalPlaceholder ?: Lang.get(player, "gui.common.none", "none")),
                    Lang.get(player, "gui.common.click_change", "Click to change"),
                    Lang.get(player, "gui.display_edit.right_click_remove", "Right-Click to remove")
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editCondition(player, name, returnCallback)
                    type.isRightClick() -> {
                        DisplayManager.updateCondition(name, null, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    }
                }
            }

            setSlot(32, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.display_edit.type_settings", "Type Settings"),
                item = GuiUtils.getDisplayIcon(display.type),
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.type", "Type") to display.type.javaClass.simpleName,
                    Lang.get(player, "gui.display_edit.click_edit_type_settings", "Click to edit type-specific settings")
                )
            )) { _, _, _, _ ->
                when (display.type) {
                    is TextDisplay -> TextDisplayEditor.open(player, name)
                    is ItemDisplay -> ItemDisplayEditor.open(player, name)
                    is BlockDisplay -> BlockDisplayEditor.open(player, name)
                    is EntityDisplay -> EntityDisplayEditor.open(player, name)
                }
            }

            GuiUtils.setupBackButton(this, 36) { returnCallback() }
            open()
        }
    }

    private fun editScale(player: ServerPlayer, name: String, currentScale: Vector3f?, isEntity: Boolean, returnCallback: () -> Unit) {
        if (isEntity) {
            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_scale", "Enter Scale"), currentScale?.x?.toString() ?: "1.0",
                onSubmit = { x ->
                    DisplayManager.updateScale(
                        name,
                        Vector3f(x.toFloat()),
                        player.createCommandSourceStack()
                    )
                    open(player, name, returnCallback)
                },
                onCancel = { open(player, name, returnCallback) }
            )
        } else {
            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_x_scale", "Enter X Scale"), currentScale?.x?.toString() ?: "1.0",
                onSubmit = { x ->
                    AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_y_scale", "Enter Y Scale"), currentScale?.y?.toString() ?: "1.0",
                        onSubmit = { y ->
                            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_z_scale", "Enter Z Scale"), currentScale?.z?.toString() ?: "1.0",
                                onSubmit = { z ->
                                    DisplayManager.updateScale(
                                        name,
                                        Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                        player.createCommandSourceStack()
                                    )
                                    open(player, name, returnCallback)
                                },
                                onCancel = { open(player, name, returnCallback) }
                            )
                        },
                        onCancel = { open(player, name, returnCallback) }
                    )
                },
                onCancel = { open(player, name, returnCallback) }
            )
        }
    }

    private fun editRotation(player: ServerPlayer, name: String, currentRotation: Vector3f?, isEntity: Boolean, returnCallback: () -> Unit) {
        if (isEntity) {
            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_pitch", "Enter Pitch"), currentRotation?.x?.toString() ?: "0",
                onSubmit = { pitch ->
                    AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_yaw", "Enter Yaw"), currentRotation?.y?.toString() ?: "0",
                        onSubmit = { yaw ->
                            DisplayManager.updateRotation(
                                name,
                                pitch.toFloat(),
                                yaw.toFloat(),
                                0f,
                                player.createCommandSourceStack()
                            )
                            open(player, name, returnCallback)
                        },
                        onCancel = { open(player, name, returnCallback) }
                    )
                },
                onCancel = { open(player, name, returnCallback) }
            )
        } else {
            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_pitch", "Enter Pitch"), currentRotation?.x?.toString() ?: "0",
                onSubmit = { pitch ->
                    AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_yaw", "Enter Yaw"), currentRotation?.y?.toString() ?: "0",
                        onSubmit = { yaw ->
                            AnvilInput.open(player, Lang.get(player, "gui.display_edit.enter_roll", "Enter Roll"), currentRotation?.z?.toString() ?: "0",
                                onSubmit = { roll ->
                                    DisplayManager.updateRotation(
                                        name,
                                        pitch.toFloat(),
                                        yaw.toFloat(),
                                        roll.toFloat(),
                                        player.createCommandSourceStack()
                                    )
                                    open(player, name, returnCallback)
                                },
                                onCancel = { open(player, name, returnCallback) }
                            )
                        },
                        onCancel = { open(player, name, returnCallback) }
                    )
                },
                onCancel = { open(player, name, returnCallback) }
            )
        }
    }

    private fun editCondition(player: ServerPlayer, name: String, returnCallback: () -> Unit) {
        AnvilInput.open(
            player = player,
            title = Lang.get(player, "gui.display_edit.enter_value_1", "Enter Value 1"),
            defaultText = "%player:name%",
            onSubmit = { placeholder ->
                AnvilInput.open(
                    player = player,
                    title = Lang.get(player, "gui.display_edit.enter_operator", "Enter Operator"),
                    defaultText = "=",
                    onSubmit = { operator ->
                        AnvilInput.open(
                            player = player,
                            title = Lang.get(player, "gui.display_edit.enter_value_2", "Enter Value 2"),
                            defaultText = "Furq_",
                            onSubmit = { target ->
                                val condition = "$placeholder $operator $target"
                                DisplayManager.updateCondition(name, condition, player.createCommandSourceStack())
                                open(player, name, returnCallback)
                            },
                            onCancel = { open(player, name, returnCallback) }
                        )
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            },
            onCancel = { open(player, name, returnCallback) }
        )
    }
}