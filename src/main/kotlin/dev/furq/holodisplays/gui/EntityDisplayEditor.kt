package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.entity.EntityPose
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

object EntityDisplayEditor {
    private val poseModes = listOf("standing", "fall_flying", "sleeping", "swimming", "spin_attack", "crouching", "long_jumping", "dying", "croaking", "using_tongue", "sitting", "roaring", "sniffing", "emerging", "digging", "sliding", "shooting", "inhaling")

    fun open(
        player: ServerPlayerEntity,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? EntityDisplay ?: return
        val gui = GuiUtils.createGui(
            type = ScreenHandlerType.GENERIC_9X3,
            player = player,
            title = "Edit Entity Display",
            size = 27,
            borderSlots = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
        )

        gui.apply {
            setSlot(11, GuiUtils.createGuiItem(
                name = "Entity",
                item = Items.ARMOR_STAND,
                lore = GuiUtils.createCombinedLore("Current" to display.id, "Click to change")
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = "Enter Entity ID",
                    defaultText = display.id,
                    onSubmit = { entityId ->
                        DisplayManager.updateEntityId(name, entityId, player.commandSource)
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(13, GuiUtils.createGuiItem(
                name = "Glow",
                item = if (display.glow == true) Items.GLOWSTONE_DUST else Items.GUNPOWDER,
                lore = GuiUtils.createCombinedLore(
                    "Current" to (display.glow?.toString() ?: "false"),
                    "Click to toggle"
                )
            )) { _, _, _, _ ->
                val newGlow = !(display.glow ?: false)
                DisplayManager.updateEntityGlow(name, newGlow, player.commandSource)
                open(player, name, returnCallback)
            }

            setSlot(15, GuiUtils.createGuiItem(
                name = "Pose",
                item = Items.ARMOR_STAND,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore("Current", display.pose?.name?.uppercase() ?: "STANDING"))
                    addAll(GuiUtils.createActionLore("Click to cycle through modes"))
                    addAll(GuiUtils.createLore(
                        "Available poses:",
                        "STANDING, FALL_FLYING, SLEEPING, SWIMMING, SPIN_ATTACK",
                        "CROUCHING, LONG_JUMPING, DYING, CROAKING, USING_TONGUE",
                        "SITTING, ROARING, SNIFFING, EMERGING, DIGGING",
                        "SLIDING, SHOOTING, INHALING"
                    ))
                }
            )) { _, _, _, _ ->
                val currentMode = display.pose?.name?.lowercase() ?: "STANDING"
                val currentIndex = poseModes.indexOf(currentMode)
                val nextMode = poseModes[(currentIndex + 1) % poseModes.size]
                val nextPose = EntityPose.valueOf(nextMode.uppercase())
                DisplayManager.updateEntityPose(name, nextPose, player.commandSource)
                open(player, name, returnCallback)
            }

            GuiUtils.setupBackButton(this, 22) { returnCallback() }
            open()
        }
    }
}