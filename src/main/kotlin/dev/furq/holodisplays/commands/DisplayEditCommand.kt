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
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.Utils
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object DisplayEditCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
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
                CommandManager.literal("lineWidth")
                    .then(CommandManager.argument("width", IntegerArgumentType.integer(1, 200))
                        .executes { context -> executeLineWidth(context) })
            )
            .then(CommandManager.literal("backgroundColor")
                .then(CommandManager.argument("color", StringArgumentType.word())
                    .suggests { _, builder ->
                        builder.suggest("FFFFFF")
                        builder.buildFuture()
                    }
                    .then(CommandManager.argument("opacity", IntegerArgumentType.integer(1, 100))
                        .executes { context -> executeBackgroundColor(context) })
                )
                .then(CommandManager.literal("default")
                    .executes { context -> executeResetBackground(context) })
            )
            .then(
                CommandManager.literal("textOpacity")
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
        )
        .then(CommandManager.literal("block")
            .then(CommandManager.literal("id")
                .then(CommandManager.argument("blockId", StringArgumentType.greedyString())
                    .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                    .executes { context -> executeBlockId(context) })
            )
        )

    private fun executeEdit(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorMessages.ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }
        val player = context.source.player ?: return 0
        DisplayEdit.open(player, name)
        return 1
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val x = FloatArgumentType.getFloat(context, "x")
        val y = FloatArgumentType.getFloat(context, "y")
        val z = FloatArgumentType.getFloat(context, "z")
        return if (Utils.updateDisplayScale(name, Vector3f(x, y, z), context.source)) 1 else 0
    }

    private fun executeResetScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return if (Utils.resetDisplayScale(name, context.source)) 1 else 0
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val mode = StringArgumentType.getString(context, "mode")
        return if (Utils.updateDisplayBillboard(name, mode, context.source)) 1 else 0
    }

    private fun executeResetBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return if (Utils.resetDisplayBillboard(name, context.source)) 1 else 0
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")
        val roll = FloatArgumentType.getFloat(context, "roll")
        return if (Utils.updateDisplayRotation(name, pitch, yaw, roll, context.source)) 1 else 0
    }

    private fun executeResetRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return if (Utils.resetDisplayRotation(name, context.source)) 1 else 0
    }

    private fun executeAddTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay ?: return 0
        val lines = display.lines.toMutableList()
        lines.add(content)
        return if (Utils.updateDisplayText(name, content, context.source)) 1 else 0
    }

    private fun executeEditTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay ?: return 0
        if (lineIndex >= display.lines.size) return 0
        val lines = display.lines.toMutableList()
        lines[lineIndex] = content
        return if (Utils.updateDisplayText(name, content, context.source)) 1 else 0
    }

    private fun executeDeleteTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val display = DisplayConfig.getDisplay(name)?.display as? TextDisplay ?: return 0
        if (lineIndex >= display.lines.size) return 0
        val lines = display.lines.toMutableList()
        lines.removeAt(lineIndex)
        return if (Utils.updateDisplayText(name, lines.joinToString("\n"), context.source)) 1 else 0
    }

    private fun executeLineWidth(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val width = IntegerArgumentType.getInteger(context, "width")
        return if (Utils.updateDisplayLineWidth(name, width, context.source)) 1 else 0
    }

    private fun executeBackgroundColor(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val color = StringArgumentType.getString(context, "color")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        return if (Utils.updateDisplayBackground(name, color, opacity, context.source)) 1 else 0
    }

    private fun executeResetBackground(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return if (Utils.resetDisplayBackground(name, context.source)) 1 else 0
    }

    private fun executeTextOpacity(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val opacity = IntegerArgumentType.getInteger(context, "opacity")
        return if (Utils.updateDisplayTextOpacity(name, opacity, context.source)) 1 else 0
    }

    private fun executeShadow(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        return if (Utils.updateDisplayShadow(name, enabled, context.source)) 1 else 0
    }

    private fun executeSeeThrough(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val enabled = BoolArgumentType.getBool(context, "enabled")
        return if (Utils.updateDisplaySeeThrough(name, enabled, context.source)) 1 else 0
    }

    private fun executeAlignment(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val align = StringArgumentType.getString(context, "align")
        return if (Utils.updateDisplayAlignment(name, align, context.source)) 1 else 0
    }

    private fun executeItemId(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")
        return if (Utils.updateDisplayItem(name, itemId, context.source)) 1 else 0
    }

    private fun executeItemDisplayType(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val type = StringArgumentType.getString(context, "type")
        return if (Utils.updateItemDisplayType(name, type, context.source)) 1 else 0
    }

    private fun executeBlockId(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")
        return if (Utils.updateDisplayBlock(name, blockId, context.source)) 1 else 0
    }
}