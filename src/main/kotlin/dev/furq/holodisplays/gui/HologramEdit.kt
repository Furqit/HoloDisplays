package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import dev.furq.holodisplays.utils.GuiUtils.isRightClick
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

object HologramEdit {
    private val billboardModes = listOf("HORIZONTAL", "VERTICAL", "CENTER", "FIXED")

    fun open(player: ServerPlayer, name: String) {
        val hologram = HologramConfig.getHologram(name) ?: return
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x5,
            player = player,
            title = Lang.get(player, "gui.hologram_edit.title", "Edit Hologram"),
            size = 45,
            borderSlots = listOf(10, 12, 14, 16, 28, 30, 32, 34, 36)
        )

        gui.apply {
            setSlot(10, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.position", "Position"),
                item = Items.ENDER_PEARL,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to "${hologram.position.x}, ${hologram.position.y}, ${hologram.position.z}",
                    Lang.get(player, "gui.hologram_edit.left_click_enter_manually", "Left-Click to enter manually"),
                    Lang.get(player, "gui.hologram_edit.right_click_current_position", "Right-Click to set to current position")
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editPosition(player, name, hologram.position.toVec3f())
                    type.isRightClick() -> setCurrentPosition(player, name)
                }
            }

            setSlot(12, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.view_range", "View Range"),
                item = Items.SPYGLASS,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to Lang.get(player, "gui.hologram_edit.blocks", "{value} blocks", "value" to hologram.viewRange),
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ ->
                AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_view_range", "Enter View Range"), hologram.viewRange.toString(),
                    onSubmit = { range ->
                        HologramManager.updateViewRange(name, range.toFloat(), player.createCommandSourceStack())
                        open(player, name)
                    },
                    onCancel = { open(player, name) }
                )
            }

            setSlot(14, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.scale", "Scale"),
                item = Items.SCAFFOLDING,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to "${hologram.scale.x}, ${hologram.scale.y}, ${hologram.scale.z}",
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ -> editScale(player, name, hologram.scale) }

            setSlot(16, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.rotation", "Rotation"),
                item = Items.COMPASS,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to "${hologram.rotation.x}°, ${hologram.rotation.y}°, ${hologram.rotation.z}°",
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ -> editRotation(player, name, hologram.rotation) }

            setSlot(28, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.billboard_mode", "Billboard Mode"),
                item = Items.COMPASS,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore(Lang.get(player, "gui.common.current", "Current"), Lang.enumHint(player, "billboard", hologram.billboardMode.name.uppercase())))
                    addAll(GuiUtils.createActionLore(Lang.get(player, "gui.common.click_cycle", "Click to cycle through modes")))
                    addAll(GuiUtils.createLore("Available modes: HORIZONTAL, VERTICAL, CENTER, FIXED"))
                }
            )) { _, _, _, _ ->
                val currentIndex = billboardModes.indexOf(hologram.billboardMode.name)
                val nextMode = billboardModes[(currentIndex + 1) % billboardModes.size]
                HologramManager.updateBillboard(name, nextMode, player.createCommandSourceStack())
                open(player, name)
            }

            setSlot(30, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.update_rate", "Update Rate"),
                item = Items.CLOCK,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to Lang.get(player, "gui.hologram_edit.ticks", "{value} ticks", "value" to hologram.updateRate),
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ ->
                AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_update_rate", "Enter Update Rate (ticks)"), hologram.updateRate.toString(),
                    onSubmit = { ticks ->
                        HologramManager.updateUpdateRate(name, ticks.toInt(), player.createCommandSourceStack())
                        open(player, name)
                    },
                    onCancel = { open(player, name) }
                )
            }

            setSlot(32, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.manage_displays", "Manage Displays"),
                item = Items.BOOK,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.hologram_edit.current_displays", "Current Displays") to hologram.displays.size.toString(),
                    Lang.get(player, "gui.hologram_edit.click_manage", "Click to manage")
                )
            )) { _, _, _, _ -> HologramDisplays.open(player, name) }

            setSlot(34, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.hologram_edit.condition", "Condition"),
                item = Items.REPEATER,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to (hologram.conditionalPlaceholder ?: Lang.get(player, "gui.common.none", "none")),
                    Lang.get(player, "gui.common.click_change", "Click to change"),
                    Lang.get(player, "gui.hologram_edit.right_click_remove", "Right-Click to remove")
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editCondition(player, name)
                    type.isRightClick() -> {
                        HologramManager.updateCondition(name, null, player.createCommandSourceStack())
                        open(player, name)
                    }
                }
            }

            GuiUtils.setupBackButton(this, 36) { HologramList.open(player) }
            open()
        }
    }

    private fun editPosition(player: ServerPlayer, name: String, currentPosition: Vector3f) {
        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_x_position", "Enter X Position"), currentPosition.x.toString(),
            onSubmit = { x ->
                AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_y_position", "Enter Y Position"), currentPosition.y.toString(),
                    onSubmit = { y ->
                        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_z_position", "Enter Z Position"), currentPosition.z.toString(),
                            onSubmit = { z ->
                                HologramManager.updatePosition(
                                    name,
                                    Vec3(x.toDouble(), y.toDouble(), z.toDouble()),
                                    //~ if >=1.21.11 'location' -> 'identifier'
                                    player.level().dimension().identifier().toString(),
                                    player.createCommandSourceStack()
                                )
                                open(player, name)
                            },
                            onCancel = { open(player, name) }
                        )
                    },
                    onCancel = { open(player, name) }
                )
            },
            onCancel = { open(player, name) }
        )
    }

    private fun setCurrentPosition(player: ServerPlayer, name: String) {
        HologramManager.updatePosition(
            name,
            player.position(),
            //~ if >=1.21.11 'location' -> 'identifier'
            player.level().dimension().identifier().toString(),
            player.createCommandSourceStack()
        )
        open(player, name)
    }

    private fun editScale(player: ServerPlayer, name: String, currentScale: Vector3f) {
        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_x_scale", "Enter X Scale"), currentScale.x.toString(),
            onSubmit = { x ->
                AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_y_scale", "Enter Y Scale"), currentScale.y.toString(),
                    onSubmit = { y ->
                        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_z_scale", "Enter Z Scale"), currentScale.z.toString(),
                            onSubmit = { z ->
                                HologramManager.updateScale(
                                    name,
                                    Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                    player.createCommandSourceStack()
                                )
                                open(player, name)
                            },
                            onCancel = { open(player, name) }
                        )
                    },
                    onCancel = { open(player, name) }
                )
            },
            onCancel = { open(player, name) }
        )
    }

    private fun editRotation(player: ServerPlayer, name: String, currentRotation: Vector3f) {
        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_pitch", "Enter Pitch"), currentRotation.x.toString(),
            onSubmit = { pitch ->
                AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_yaw", "Enter Yaw"), currentRotation.y.toString(),
                    onSubmit = { yaw ->
                        AnvilInput.open(player, Lang.get(player, "gui.hologram_edit.enter_roll", "Enter Roll"), currentRotation.z.toString(),
                            onSubmit = { roll ->
                                HologramManager.updateRotation(
                                    name,
                                    pitch.toFloat(),
                                    yaw.toFloat(),
                                    roll.toFloat(),
                                    player.createCommandSourceStack()
                                )
                                open(player, name)
                            },
                            onCancel = { open(player, name) }
                        )
                    },
                    onCancel = { open(player, name) }
                )
            },
            onCancel = { open(player, name) }
        )
    }

    private fun editCondition(player: ServerPlayer, name: String) {
        AnvilInput.open(
            player = player,
            title = Lang.get(player, "gui.hologram_edit.enter_value_1", "Enter Value 1"),
            defaultText = "%player:name%",
            onSubmit = { placeholder ->
                AnvilInput.open(
                    player = player,
                    title = Lang.get(player, "gui.hologram_edit.enter_operator", "Enter Operator"),
                    defaultText = "=",
                    onSubmit = { operator ->
                        AnvilInput.open(
                            player = player,
                            title = Lang.get(player, "gui.hologram_edit.enter_value_2", "Enter Value 2"),
                            defaultText = "Furq_",
                            onSubmit = { target ->
                                val condition = "$placeholder $operator $target"
                                HologramManager.updateCondition(name, condition, player.createCommandSourceStack())
                                open(player, name)
                            },
                            onCancel = { open(player, name) }
                        )
                    },
                    onCancel = { open(player, name) }
                )
            },
            onCancel = { open(player, name) }
        )
    }
}