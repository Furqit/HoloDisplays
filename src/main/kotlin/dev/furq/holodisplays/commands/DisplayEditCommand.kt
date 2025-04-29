package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
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
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object DisplayEditCommand {
    private val displayManager = DisplayManager()

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("edit")
        .then(CommandManager.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
            .executes { context -> executeEdit(context) }
            .then(
                CommandManager.literal("scale")
                    .then(
                        CommandManager.argument("x", FloatArgumentType.floatArg(0.1f))
                            .then(CommandManager.argument("y", FloatArgumentType.floatArg(0.1f))
                                .then(CommandManager.argument("z", FloatArgumentType.floatArg(0.1f))
                                    .executes { context -> executeScale(context) }
                                )
                            )
                    )
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetScale(context) })
            )
            .then(CommandManager.literal("billboard")
                .then(CommandManager.argument("mode", StringArgumentType.word())
                    .suggests { _, builder ->
                        BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                        builder.buildFuture()
                    }
                    .executes { context -> executeBillboard(context) })
                .then(CommandManager.literal("reset")
                    .executes { context -> executeResetBillboard(context) })
            )
            .then(
                CommandManager.literal("rotation")
                    .then(
                        CommandManager.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                            .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                                .then(CommandManager.argument("roll", FloatArgumentType.floatArg(-180f, 180f))
                                    .executes { context -> executeRotation(context) }
                                )
                            )
                    )
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetRotation(context) })
            )
            .then(CommandManager.literal("text")
                .then(
                    CommandManager.literal("line")
                        .then(
                            CommandManager.literal("add")
                                .then(CommandManager.argument("content", StringArgumentType.greedyString())
                                    .executes { context -> executeAddTextLine(context) })
                        )
                        .then(
                            CommandManager.argument("lineIndex", IntegerArgumentType.integer(0))
                                .then(CommandManager.argument("content", StringArgumentType.greedyString())
                                    .executes { context -> executeEditTextLine(context) })
                        )
                        .then(
                            CommandManager.literal("delete")
                                .then(CommandManager.argument("lineIndex", IntegerArgumentType.integer(0))
                                    .executes { context -> executeDeleteTextLine(context) })
                        )
                )
                .then(
                    CommandManager.literal("width")
                        .then(CommandManager.argument("width", IntegerArgumentType.integer(1, 200))
                            .executes { context -> executeLineWidth(context) })
                )
                .then(CommandManager.literal("background")
                    .then(CommandManager.argument("color", StringArgumentType.word())
                        .suggests { _, builder ->
                            builder.suggest("FFFFFF")
                            builder.buildFuture()
                        }
                        .then(CommandManager.argument("opacity", IntegerArgumentType.integer(1, 100))
                            .executes { context -> executeBackgroundColor(context) })
                    )
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetBackground(context) })
                )
                .then(
                    CommandManager.literal("opacity")
                        .then(CommandManager.argument("opacity", IntegerArgumentType.integer(1, 100))
                            .executes { context -> executeTextOpacity(context) })
                )
                .then(
                    CommandManager.literal("shadow")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes { context -> executeShadow(context) })
                )
                .then(
                    CommandManager.literal("seeThrough")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes { context -> executeSeeThrough(context) })
                )
                .then(CommandManager.literal("alignment")
                    .then(CommandManager.argument("align", StringArgumentType.word())
                        .suggests { _, builder ->
                            TextDisplay.TextAlignment.entries.forEach {
                                builder.suggest(it.name.lowercase())
                            }
                            builder.buildFuture()
                        }
                        .executes { context -> executeAlignment(context) }
                    )
                )
            )
            .then(CommandManager.literal("item")
                .then(CommandManager.literal("id")
                    .then(CommandManager.argument("itemId", StringArgumentType.greedyString())
                        .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                        .executes { context -> executeItemId(context) })
                )
                .then(CommandManager.literal("displayType")
                    .then(CommandManager.argument("type", StringArgumentType.word())
                        .suggests { _, builder ->
                            listOf(
                                "none", "thirdperson_lefthand", "thirdperson_righthand",
                                "firstperson_lefthand", "firstperson_righthand", "head",
                                "gui", "ground", "fixed"
                            ).forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { context -> executeItemDisplayType(context) })
                )
                .then(CommandManager.literal("customModelData")
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(1))
                        .executes { context -> executeCustomModelData(context) }
                    )
                    .then(CommandManager.literal("reset")
                        .executes { context -> executeResetCustomModelData(context) }
                    )
                )
            )
            .then(CommandManager.literal("block")
                .then(CommandManager.literal("id")
                    .then(CommandManager.argument("blockId", StringArgumentType.greedyString())
                        .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                        .executes { context -> executeBlockId(context) })
                )
            )
            .then(
                CommandManager.literal("condition")
                    .then(
                        CommandManager.argument("condition", StringArgumentType.greedyString())
                            .executes { context -> executeCondition(context) }
                    )
                    .then(
                        CommandManager.literal("remove")
                            .executes { context -> executeConditionRemove(context) }
                    )
            )
        )

    private fun executeEdit(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return 0
        }
        DisplayEdit.open(context.source.player!!, name)
        return 1
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")
        displayManager.updateScale(name, Vector3f(x, y, z), context.source)
        return 1
    }

    private fun executeResetScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.resetScale(name, context.source)
        return 1
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode")
        displayManager.updateBillboard(name, mode, context.source)
        return 1
    }

    private fun executeResetBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.resetBillboard(name, context.source)
        return 1
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")

        if (pitch < -180f || pitch > 180f || yaw < -180f || yaw > 180f || roll < -180f || roll > 180f) {
            FeedbackManager.send(context.source, FeedbackType.INVALID_ROTATION)
            return 0
        }

        displayManager.updateRotation(name, pitch, yaw, roll, context.source)
        return 1
    }

    private fun executeResetRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.resetRotation(name, context.source)
        return 1
    }

    private fun executeAddTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay
        if (display == null) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return 0
        }
        val lines = display.lines.toMutableList()
        lines.add(content)
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line added")
        return 1
    }

    private fun executeEditTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay
        if (display == null) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return 0
        }
        if (lineIndex >= display.lines.size) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "invalid line index")
            return 0
        }
        val lines = display.lines.toMutableList()
        lines[lineIndex] = content
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line updated")
        return 1
    }

    private fun executeDeleteTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay
        if (display == null) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_NOT_FOUND, "name" to name)
            return 0
        }
        if (lineIndex >= display.lines.size) {
            FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "invalid line index")
            return 0
        }
        val lines = display.lines.toMutableList()
        lines.removeAt(lineIndex)
        DisplayHandler.updateDisplayProperty(name, DisplayHandler.DisplayProperty.TextLines(lines))
        FeedbackManager.send(context.source, FeedbackType.DISPLAY_UPDATED, "detail" to "text line removed")
        return 1
    }

    private fun executeLineWidth(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val width = IntegerArgumentType.getInteger(context, "width")
        displayManager.updateLineWidth(name, width, context.source)
        return 1
    }

    private fun executeBackgroundColor(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val color = StringArgumentType.getString(context, "color")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        displayManager.updateBackground(name, color, opacity, context.source)
        return 1
    }

    private fun executeResetBackground(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.resetBackground(name, context.source)
        return 1
    }

    private fun executeTextOpacity(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        displayManager.updateTextOpacity(name, opacity, context.source)
        return 1
    }

    private fun executeShadow(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        displayManager.updateShadow(name, enabled, context.source)
        return 1
    }

    private fun executeSeeThrough(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        displayManager.updateSeeThrough(name, enabled, context.source)
        return 1
    }

    private fun executeAlignment(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val alignment = StringArgumentType.getString(context, "align")
        displayManager.updateAlignment(name, alignment, context.source)
        return 1
    }

    private fun executeItemId(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")
        displayManager.updateItemId(name, itemId, context.source)
        return 1
    }

    private fun executeItemDisplayType(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val type = StringArgumentType.getString(context, "type")
        displayManager.updateItemDisplayType(name, type, context.source)
        return 1
    }

    private fun executeCustomModelData(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val value = IntegerArgumentType.getInteger(context, "value")
        displayManager.updateCustomModelData(name, value, context.source)
        return 1
    }

    private fun executeResetCustomModelData(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.updateCustomModelData(name, null, context.source)
        return 1
    }

    private fun executeBlockId(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")
        displayManager.updateBlockId(name, blockId, context.source)
        return 1
    }

    private fun executeCondition(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val condition = StringArgumentType.getString(context, "condition")

        if (!condition.matches(Regex("^%[\\w:]+% *(=|!=|>|<|>=|<=|contains|!contains|startsWith|endsWith) *\\S+$"))) {
            FeedbackManager.send(context.source, FeedbackType.INVALID_CONDITION)
            return 0
        }

        displayManager.updateCondition(name, condition, context.source)
        return 1
    }

    private fun executeConditionRemove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        displayManager.updateCondition(name, null, context.source)
        return 1
    }
}