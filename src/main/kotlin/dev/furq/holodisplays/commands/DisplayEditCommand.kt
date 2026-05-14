package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.gui.DisplayEdit
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose
import org.joml.Vector3f

object DisplayEditCommand : EditCommand() {
    override fun updateScale(name: String, scale: Vector3f, source: CommandSourceStack) = DisplayManager.updateScale(name, scale, source)
    override fun resetScale(name: String, source: CommandSourceStack) = DisplayManager.updateScale(name, null, source)
    override fun updateBillboard(name: String, mode: String, source: CommandSourceStack) = DisplayManager.updateBillboard(name, mode, source)
    override fun resetBillboard(name: String, source: CommandSourceStack) = DisplayManager.updateRotation(name, null, null, null, source)
    override fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: CommandSourceStack) = DisplayManager.updateRotation(name, pitch, yaw, roll, source)
    override fun resetRotation(name: String, source: CommandSourceStack) = DisplayManager.updateRotation(name, null, null, null, source)
    override fun updateCondition(name: String, condition: String?, source: CommandSourceStack) = DisplayManager.updateCondition(name, condition, source)
    override fun openEditGui(player: ServerPlayer, name: String) = DisplayEdit.open(player, name)

    fun register(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("edit")
        .then(Commands.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
            .executes { context -> executeEdit(context) }
            .then(buildScaleCommands())
            .then(buildBillboardCommands())
            .then(buildRotationCommands())
            .then(buildTextCommands())
            .then(buildItemCommands())
            .then(buildBlockCommands())
            .then(buildEntityCommands())
            .then(buildConditionCommands())
        )

    private fun buildTextCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("text")
            .then(buildTextLineCommands())
            .then(Commands.literal("width")
                .then(Commands.argument("width", IntegerArgumentType.integer(1, 200))
                    .executes { context -> executeLineWidth(context) }))
            .then(buildBackgroundCommands())
            .then(Commands.literal("opacity")
                .then(Commands.argument("opacity", IntegerArgumentType.integer(1, 100))
                    .executes { context -> executeTextOpacity(context) }))
            .then(Commands.literal("shadow")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes { context -> executeShadow(context) }))
            .then(Commands.literal("seeThrough")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes { context -> executeSeeThrough(context) }))
            .then(Commands.literal("alignment")
                .then(Commands.argument("align", StringArgumentType.word())
                    .suggests { _, builder ->
                        TextDisplay.TextAlignment.entries.forEach { builder.suggest(it.name.lowercase()) }
                        builder.buildFuture()
                    }
                    .executes { context -> executeAlignment(context) }))
    }

    private fun buildTextLineCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("line")
            .then(Commands.literal("add")
                .then(Commands.argument("content", StringArgumentType.greedyString())
                    .executes { context -> executeAddTextLine(context) }))
            .then(Commands.literal("set")
                .then(Commands.argument("lineIndex", IntegerArgumentType.integer(0))
                    .then(Commands.argument("content", StringArgumentType.greedyString())
                        .executes { context -> executeEditTextLine(context) })))
            .then(Commands.literal("delete")
                .then(Commands.argument("lineIndex", IntegerArgumentType.integer(0))
                    .executes { context -> executeDeleteTextLine(context) }))
    }

    private fun buildBackgroundCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("background")
            .then(Commands.argument("color", StringArgumentType.word())
                .suggests { _, builder ->
                    builder.suggest("FFFFFF")
                    builder.buildFuture()
                }
                .then(Commands.argument("opacity", IntegerArgumentType.integer(0, 100))
                    .executes { context -> executeBackgroundColor(context) }))
            .then(Commands.literal("reset")
                .executes { context -> executeResetBackground(context) })
    }

    private fun buildItemCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("item")
            .then(Commands.literal("id")
                .then(Commands.argument("itemId", StringArgumentType.greedyString())
                    .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                    .executes { context -> executeItemId(context) }))
            .then(Commands.literal("displayType")
                .then(Commands.argument("type", StringArgumentType.word())
                    .suggests { _, builder ->
                        listOf("none", "thirdperson_lefthand", "thirdperson_righthand",
                            "firstperson_lefthand", "firstperson_righthand", "head",
                            "gui", "ground", "fixed").forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .executes { context -> executeItemDisplayType(context) }))
            .then(Commands.literal("customModelData")
                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                    .executes { context -> executeCustomModelData(context) })
                .then(Commands.literal("reset")
                    .executes { context -> executeResetCustomModelData(context) }))
    }

    private fun buildBlockCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("block")
            .then(Commands.literal("id")
                .then(Commands.argument("blockId", StringArgumentType.greedyString())
                    .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                    .executes { context -> executeBlockId(context) }))
    }

    private fun buildEntityCommands(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.literal("entity")
            .then(Commands.literal("id")
                .then(Commands.argument("entityId", StringArgumentType.greedyString())
                    .suggests { _, builder -> CommandUtils.suggestEntityIds(builder) }
                    .executes { context -> executeEntityId(context) }))
            .then(Commands.literal("glow")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes { context -> executeEntityGlow(context) }))
            .then(Commands.literal("pose")
                .then(Commands.argument("pose", StringArgumentType.greedyString())
                    .executes { context -> executeEntityPose(context) })
                .then(Commands.literal("reset")
                    .executes { context -> executeResetEntityPose(context) }))
    }

    private fun executeAddTextLine(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return 0
        val lines = display.lines.toMutableList().apply { add(content) }
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line added")
        return 1
    }

    private fun executeEditTextLine(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return 0
        if (lineIndex >= display.lines.size) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "invalid line index")
            return 0
        }
        val lines = display.lines.toMutableList().apply { this[lineIndex] = content }
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line updated")
        return 1
    }

    private fun executeDeleteTextLine(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val display = DisplayConfig.getDisplay(name)?.type as? TextDisplay ?: return 0
        if (lineIndex >= display.lines.size) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "invalid line index")
            return 0
        }
        val lines = display.lines.toMutableList().apply { removeAt(lineIndex) }
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line removed")
        return 1
    }

    private fun executeLineWidth(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val width = IntegerArgumentType.getInteger(context, "width")
        DisplayManager.updateLineWidth(name, width, context.source)
        return 1
    }

    private fun executeBackgroundColor(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val color = StringArgumentType.getString(context, "color")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        DisplayManager.updateBackground(name, color, opacity, context.source)
        return 1
    }

    private fun executeResetBackground(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        DisplayManager.updateBackground(name, null, null, context.source)
        return 1
    }

    private fun executeTextOpacity(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        DisplayManager.updateTextOpacity(name, opacity, context.source)
        return 1
    }

    private fun executeShadow(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        DisplayManager.updateShadow(name, enabled, context.source)
        return 1
    }

    private fun executeSeeThrough(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        DisplayManager.updateSeeThrough(name, enabled, context.source)
        return 1
    }

    private fun executeAlignment(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val alignment = StringArgumentType.getString(context, "align")
        DisplayManager.updateAlignment(name, alignment, context.source)
        return 1
    }

    private fun executeItemId(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")
        DisplayManager.updateItemId(name, itemId, context.source)
        return 1
    }

    private fun executeItemDisplayType(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val type = StringArgumentType.getString(context, "type")
        DisplayManager.updateItemDisplayType(name, type, context.source)
        return 1
    }

    private fun executeCustomModelData(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val value = IntegerArgumentType.getInteger(context, "value")
        DisplayManager.updateCustomModelData(name, value, context.source)
        return 1
    }

    private fun executeResetCustomModelData(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        DisplayManager.updateCustomModelData(name, null, context.source)
        return 1
    }

    private fun executeBlockId(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")
        DisplayManager.updateBlockId(name, blockId, context.source)
        return 1
    }

    private fun executeEntityId(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val entityId = StringArgumentType.getString(context, "entityId")
        DisplayManager.updateEntityId(name, entityId, context.source)
        return 1
    }

    private fun executeEntityGlow(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        DisplayManager.updateEntityGlow(name, enabled, context.source)
        return 1
    }

    private fun executeEntityPose(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val poseString = StringArgumentType.getString(context, "pose")
        val pose = Pose.valueOf(poseString.uppercase())
        DisplayManager.updateEntityPose(name, pose, context.source)
        return 1
    }

    private fun executeResetEntityPose(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        DisplayManager.updateEntityPose(name, null, context.source)
        return 1
    }
}