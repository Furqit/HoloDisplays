package dev.furq.holodisplays.gui

import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.Lang
import dev.furq.holodisplays.data.display.EntityDisplay
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.utils.GuiUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Items

object EntityDisplayEditor {
    private val poseModes = listOf("standing", "fall_flying", "sleeping", "swimming", "spin_attack", "crouching", "long_jumping", "dying", "croaking", "using_tongue", "sitting", "roaring", "sniffing", "emerging", "digging", "sliding", "shooting", "inhaling")

    fun open(
        player: ServerPlayer,
        name: String,
        returnCallback: () -> Unit = { DisplayEdit.open(player, name) }
    ) {
        val display = DisplayConfig.getDisplay(name)?.type as? EntityDisplay ?: return
        val gui = GuiUtils.createGui(
            type = MenuType.GENERIC_9x3,
            player = player,
            title = Lang.get(player, "gui.entity_display_editor.title", "Edit Entity Display"),
            size = 27,
            borderSlots = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
        )

        gui.apply {
            setSlot(11, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.entity_display_editor.entity", "Entity"),
                item = Items.ARMOR_STAND,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to display.id,
                    Lang.get(player, "gui.common.click_change", "Click to change")
                )
            )) { _, _, _, _ ->
                AnvilInput.open(
                    player = player,
                    title = Lang.get(player, "gui.entity_display_editor.enter_entity_id", "Enter Entity ID"),
                    defaultText = display.id,
                    onSubmit = { entityId ->
                        DisplayManager.updateEntityId(name, entityId, player.createCommandSourceStack())
                        open(player, name, returnCallback)
                    },
                    onCancel = { open(player, name, returnCallback) }
                )
            }

            setSlot(13, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.entity_display_editor.glow", "Glow"),
                item = if (display.glow == true) Items.GLOWSTONE_DUST else Items.GUNPOWDER,
                lore = GuiUtils.createCombinedLore(
                    Lang.get(player, "gui.common.current", "Current") to (display.glow?.toString() ?: "false"),
                    Lang.get(player, "gui.common.click_toggle", "Click to toggle")
                )
            )) { _, _, _, _ ->
                val newGlow = !(display.glow ?: false)
                DisplayManager.updateEntityGlow(name, newGlow, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            setSlot(15, GuiUtils.createGuiItem(
                name = Lang.get(player, "gui.entity_display_editor.pose", "Pose"),
                item = Items.ARMOR_STAND,
                lore = buildList {
                    addAll(GuiUtils.createCurrentValueLore(Lang.get(player, "gui.common.current", "Current"), Lang.enumHint(player, "pose", display.pose?.name?.uppercase() ?: "STANDING")))
                    addAll(GuiUtils.createActionLore(Lang.get(player, "gui.common.click_cycle", "Click to cycle through modes")))
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
                val nextPose = Pose.valueOf(nextMode.uppercase())
                DisplayManager.updateEntityPose(name, nextPose, player.createCommandSourceStack())
                open(player, name, returnCallback)
            }

            GuiUtils.setupBackButton(this, 22) { returnCallback() }
            open()
        }
    }
}