package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import org.joml.Vector3f

object DisplayEdit {
    private val displayManager = DisplayManager()
    private val billboardModes = listOf("HORIZONTAL", "VERTICAL", "CENTER", "FIXED")

    fun open(player: ServerPlayerEntity, name: String, returnCallback: () -> Unit = { DisplayList.open(player) }) {
        val display = DisplayConfig.getDisplay(name) ?: return
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X5,
            player = player,
            title = "Edit Display",
            size = 45,
            borderSlots = listOf(11, 13, 15, 30, 32, 36)
        )

        gui.apply {
            setSlot(11, GuiUtils.createGuiItem(
                name = "Scale",
                item = Items.SCAFFOLDING,
                lore = GuiUtils.createCombinedLore(
                    "Current" to "${display.display.scale?.x ?: 1.0}, ${display.display.scale?.y ?: 1.0}, ${display.display.scale?.z ?: 1.0}",
                    "Click to change", "Right click to reset"
                )
            )) { _, type, _, _ ->
                when {
                    type.isRight -> {
                        displayManager.resetScale(name, player.commandSource)
                        open(player, name, returnCallback)
                    }

                    else -> editScale(player, name, display.display.scale, returnCallback)
                }
            }

            setSlot(13, GuiUtils.createGuiItem(
                name = "Billboard Mode",
                item = Items.COMPASS,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Current", display.display.billboardMode?.name?.uppercase()
                        ?: "NONE"))
                    addAll(GuiUtils.createActionLore("Click to cycle through modes", "Right click to reset"))
                    addAll(GuiUtils.createLore("Available modes: HORIZONTAL, VERTICAL, CENTER, FIXED"))
                }
            )) { _, type, _, _ ->
                when {
                    type.isRight -> {
                        displayManager.resetBillboard(name, player.commandSource)
                        open(player, name, returnCallback)
                    }

                    else -> {
                        val currentMode = display.display.billboardMode?.name ?: "FIXED"
                        val currentIndex = billboardModes.indexOf(currentMode)
                        val nextMode = billboardModes[(currentIndex + 1) % billboardModes.size]
                        displayManager.updateBillboard(name, nextMode.lowercase(), player.commandSource)
                        open(player, name, returnCallback)
                    }
                }
            }

            setSlot(15, GuiUtils.createGuiItem(
                name = "Rotation",
                item = Items.CLOCK,
                lore = GuiUtils.createCombinedLore(
                    "Current" to "${display.display.rotation?.x ?: 0}°, ${display.display.rotation?.y ?: 0}°, ${display.display.rotation?.z ?: 0}°",
                    "Click to change", "Right click to reset"
                )
            )) { _, type, _, _ ->
                when {
                    type.isRight -> {
                        displayManager.resetRotation(name, player.commandSource)
                        open(player, name, returnCallback)
                    }

                    else -> editRotation(player, name, display.display.rotation, returnCallback)
                }
            }

            setSlot(30, GuiUtils.createGuiItem(
                name = "Condition",
                item = Items.REPEATER,
                lore = GuiUtils.createCombinedLore(
                    "Current" to (display.display.conditionalPlaceholder ?: "none"),
                    "Click to change", "Right-Click to remove"
                )
            )) { _, type, _, _ ->
                when {
                    type.isLeft -> editCondition(player, name, returnCallback)
                    type.isRight -> {
                        displayManager.updateCondition(name, null, player.commandSource)
                        open(player, name, returnCallback)
                    }
                }
            }

            setSlot(32, GuiUtils.createGuiItem(
                name = "Type Settings",
                item = GuiUtils.getDisplayIcon(display.display),
                lore = GuiUtils.createCombinedLore(
                    "Type" to display.display.javaClass.simpleName,
                    "Click to edit type-specific settings"
                )
            )) { _, _, _, _ ->
                when (display.display) {
                    is dev.furq.holodisplays.data.display.TextDisplay -> TextDisplayEditor.open(player, name)
                    is dev.furq.holodisplays.data.display.ItemDisplay -> ItemDisplayEditor.open(player, name)
                    is dev.furq.holodisplays.data.display.BlockDisplay -> BlockDisplayEditor.open(player, name)
                }
            }

            GuiUtils.setupBackButton(this, 36) { returnCallback() }
            open()
        }
    }

    private fun editScale(player: ServerPlayerEntity, name: String, currentScale: Vector3f?, returnCallback: () -> Unit) {
        AnvilInput.open(player, "Enter X Scale", currentScale?.x?.toString() ?: "1.0",
            onSubmit = { x ->
                AnvilInput.open(player, "Enter Y Scale", currentScale?.y?.toString() ?: "1.0",
                    onSubmit = { y ->
                        AnvilInput.open(player, "Enter Z Scale", currentScale?.z?.toString() ?: "1.0",
                            onSubmit = { z ->
                                displayManager.updateScale(
                                    name,
                                    Vector3f(x.toFloat(), y.toFloat(), z.toFloat()),
                                    player.commandSource
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

    private fun editRotation(player: ServerPlayerEntity, name: String, currentRotation: Vector3f?, returnCallback: () -> Unit) {
        AnvilInput.open(player, "Enter Pitch", currentRotation?.x?.toString() ?: "0",
            onSubmit = { pitch ->
                AnvilInput.open(player, "Enter Yaw", currentRotation?.y?.toString() ?: "0",
                    onSubmit = { yaw ->
                        AnvilInput.open(player, "Enter Roll", currentRotation?.z?.toString() ?: "0",
                            onSubmit = { roll ->
                                displayManager.updateRotation(
                                    name,
                                    pitch.toFloat(),
                                    yaw.toFloat(),
                                    roll.toFloat(),
                                    player.commandSource
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

    private fun editCondition(player: ServerPlayerEntity, name: String, returnCallback: () -> Unit) {
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
                                displayManager.updateCondition(name, condition, player.commandSource)
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