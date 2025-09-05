package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import org.joml.Vector3f

object HologramEdit {
    private val hologramManager = HologramManager()
    private val billboardModes = listOf("HORIZONTAL", "VERTICAL", "CENTER", "FIXED")

    fun open(player: ServerPlayerEntity, name: String) {
        val hologram = HologramConfig.getHologram(name) ?: return
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = "Edit Hologram",
            size = 45,
            borderSlots = listOf(10, 12, 14, 16, 28, 30, 32, 34, 36)
        )

        gui.apply {
            setSlot(10, GuiUtils.createGuiItem(
                name = "Position",
                item = Items.ENDER_PEARL,
                lore = GuiUtils.createCombinedLore(
                    "Current" to "${hologram.position.x}, ${hologram.position.y}, ${hologram.position.z}",
                    "Left-Click to enter manually", "Right-Click to set to current position"
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editPosition(player, name, hologram.position)
                    type.isRight -> setCurrentPosition(player, name)
                }
            }

            setSlot(12, GuiUtils.createGuiItem(
                name = "View Range",
                item = Items.SPYGLASS,
                lore = GuiUtils.createCombinedLore("Current" to "${hologram.viewRange} blocks", "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(player, "Enter View Range", hologram.viewRange.toString(),
                    onSubmit = { range ->
                        hologramManager.updateViewRange(name, range.toFloat(), player.commandSource)
                        open(player, name)
                    },
                    onCancel = { open(player, name) }
                )
            }

            setSlot(14, GuiUtils.createGuiItem(
                name = "Scale",
                item = Items.SCAFFOLDING,
                lore = GuiUtils.createCombinedLore(
                    "Current" to "${hologram.scale.x}, ${hologram.scale.y}, ${hologram.scale.z}",
                    "Click to change"
                )
            )) { _, _, _, _ -> editScale(player, name, hologram.scale) }

            setSlot(16, GuiUtils.createGuiItem(
                name = "Rotation",
                item = Items.COMPASS,
                lore = GuiUtils.createCombinedLore(
                    "Current" to "${hologram.rotation.x}°, ${hologram.rotation.y}°, ${hologram.rotation.z}°",
                    "Click to change"
                )
            )) { _, _, _, _ -> editRotation(player, name, hologram.rotation) }

            setSlot(28, GuiUtils.createGuiItem(
                name = "Billboard Mode",
                item = Items.COMPASS,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Current", hologram.billboardMode.name.uppercase()))
                    addAll(GuiUtils.createActionLore("Click to cycle through modes"))
                    addAll(GuiUtils.createLore("Available modes: HORIZONTAL, VERTICAL, CENTER, FIXED"))
                }
            )) { _, _, _, _ ->
                val currentIndex = billboardModes.indexOf(hologram.billboardMode.name)
                val nextMode = billboardModes[(currentIndex + 1) % billboardModes.size]
                hologramManager.updateBillboard(name, nextMode, player.commandSource)
                open(player, name)
            }

            setSlot(30, GuiUtils.createGuiItem(
                name = "Update Rate",
                item = Items.CLOCK,
                lore = GuiUtils.createCombinedLore("Current" to "${hologram.updateRate} ticks", "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(player, "Enter Update Rate (ticks)", hologram.updateRate.toString(),
                    onSubmit = { ticks ->
                        hologramManager.updateUpdateRate(name, ticks.toInt(), player.commandSource)
                        open(player, name)
                    },
                    onCancel = { open(player, name) }
                )
            }

            setSlot(32, GuiUtils.createGuiItem(
                name = "Manage Displays",
                item = Items.BOOK,
                lore = GuiUtils.createCombinedLore("Current Displays" to hologram.displays.size.toString(), "Click to manage")
            )) { _, _, _, _ -> HologramDisplays.open(player, name) }

            setSlot(34, GuiUtils.createGuiItem(
                name = "Condition",
                item = Items.REPEATER,
                lore = GuiUtils.createCombinedLore(
                    "Current" to (hologram.conditionalPlaceholder ?: "none"),
                    "Click to change", "Right-Click to remove"
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editCondition(player, name)
                    type.isRight -> {
                        hologramManager.updateCondition(name, null, player.commandSource)
                        open(player, name)
                    }
                }
            }

            GuiUtils.setupBackButton(this, 36) { HologramList.open(player) }
            open()
        }
    }

    private fun editPosition(player: ServerPlayerEntity, name: String, currentPosition: Vector3f) {
        AnvilInput.open(player, "Enter X Position", currentPosition.x.toString(),
            onSubmit = { x ->
                AnvilInput.open(player, "Enter Y Position", currentPosition.y.toString(),
                    onSubmit = { y ->
                        AnvilInput.open(player, "Enter Z Position", currentPosition.z.toString(),
                            onSubmit = { z ->
                                hologramManager.updatePosition(
                                    name,
                                    Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                    player.world.registryKey.value.toString(),
                                    player.commandSource
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

    private fun setCurrentPosition(player: ServerPlayerEntity, name: String) {
        hologramManager.updatePosition(
            name,
            Vector3f(player.x.toFloat(), player.y.toFloat(), player.z.toFloat()),
            player.world.registryKey.value.toString(),
            player.commandSource
        )
        open(player, name)
    }

    private fun editScale(player: ServerPlayerEntity, name: String, currentScale: Vector3f) {
        AnvilInput.open(player, "Enter X Scale", currentScale.x.toString(),
            onSubmit = { x ->
                AnvilInput.open(player, "Enter Y Scale", currentScale.y.toString(),
                    onSubmit = { y ->
                        AnvilInput.open(player, "Enter Z Scale", currentScale.z.toString(),
                            onSubmit = { z ->
                                hologramManager.updateScale(
                                    name,
                                    Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                    player.commandSource
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

    private fun editRotation(player: ServerPlayerEntity, name: String, currentRotation: Vector3f) {
        AnvilInput.open(player, "Enter Pitch", currentRotation.x.toString(),
            onSubmit = { pitch ->
                AnvilInput.open(player, "Enter Yaw", currentRotation.y.toString(),
                    onSubmit = { yaw ->
                        AnvilInput.open(player, "Enter Roll", currentRotation.z.toString(),
                            onSubmit = { roll ->
                                hologramManager.updateRotation(
                                    name,
                                    pitch.toFloat(),
                                    yaw.toFloat(),
                                    roll.toFloat(),
                                    player.commandSource
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

    private fun editCondition(player: ServerPlayerEntity, name: String) {
        AnvilInput.open(
            player = player,
            title = "Enter Value 1",
            defaultText = "%player:name%",
            onSubmit = { placeholder ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Operator",
                    defaultText = "=",
                    onSubmit = { operator ->
                        AnvilInput.open(
                            player = player,
                            title = "Enter Value 2",
                            defaultText = "Furq_",
                            onSubmit = { target ->
                                val condition = "$placeholder $operator $target"
                                hologramManager.updateCondition(name, condition, player.commandSource)
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