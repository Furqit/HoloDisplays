package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.DisplayHandler
import dev.furq.holodisplays.menu.BlockEditMenu
import dev.furq.holodisplays.menu.EditMenu
import dev.furq.holodisplays.menu.ItemEditMenu
import dev.furq.holodisplays.menu.TextEditMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import dev.furq.holodisplays.utils.HandlerUtils.HologramProperty
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object DisplayEditCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
        .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
        .executes { context -> executeOpenMenu(context) }
        .then(
            CommandManager.literal("scale")
                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.1f, 10f))
                    .executes { context -> executeCommonProperty(context, "scale") })
                .then(CommandManager.literal("reset")
                    .executes { context -> executeResetCommonProperty(context, "scale") })
        )
        .then(CommandManager.literal("billboard")
            .then(CommandManager.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                    builder.buildFuture()
                }
                .executes { context -> executeCommonProperty(context, "billboard") })
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetCommonProperty(context, "billboard") })
        )
        .then(
            CommandManager.literal("rotation")
                .then(
                    CommandManager.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                        .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                            .executes { context -> executeRotation(context) })
                )
                .then(CommandManager.literal("reset")
                    .executes { context -> executeResetCommonProperty(context, "rotation") })
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
                        .executes { context -> executeTextProperty(context, "lineWidth") })
            )
            .then(CommandManager.literal("backgroundColor")
                .then(CommandManager.argument("color", StringArgumentType.word())
                    .suggests { _, builder ->
                        builder.suggest("FFFFFF")
                        builder.buildFuture()
                    }
                    .then(CommandManager.argument("opacity", IntegerArgumentType.integer(1, 100))
                        .executes { context -> executeTextProperty(context, "backgroundColor") }
                    )
                    .executes { context -> executeTextProperty(context, "backgroundColor") }
                )
                .then(CommandManager.literal("default")
                    .executes { context -> executeDefaultBackground(context) })
            )
            .then(
                CommandManager.literal("textOpacity")
                    .then(CommandManager.argument("opacity", IntegerArgumentType.integer(1, 100))
                        .executes { context -> executeTextProperty(context, "textOpacity") })
            )
            .then(
                CommandManager.literal("shadow")
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes { context -> executeTextProperty(context, "shadow") })
            )
            .then(
                CommandManager.literal("seeThrough")
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes { context -> executeTextProperty(context, "seeThrough") })
            )
            .then(CommandManager.literal("alignment")
                .then(CommandManager.argument("align", StringArgumentType.word())
                    .suggests { _, builder ->
                        DisplayData.TextAlignment.entries.forEach {
                            builder.suggest(it.name.lowercase())
                        }
                        builder.buildFuture()
                    }
                    .executes { context -> executeTextProperty(context, "alignment") })
            )
        )
        .then(CommandManager.literal("item")
            .then(CommandManager.literal("id")
                .then(CommandManager.argument("itemId", StringArgumentType.greedyString())
                    .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                    .executes { context -> executeItemProperty(context) })
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
                    .executes { context -> executeBlockProperty(context) })
            )
        )

    private fun executeCommonProperty(context: CommandContext<ServerCommandSource>, propertyName: String): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayConfig.getDisplay(name) ?: return 0
        val property = when (propertyName) {
            "scale" -> {
                val value = FloatArgumentType.getFloat(context, "value")
                if (value !in 0.1f..10f) {
                    ErrorMessages.sendError(context.source, ErrorType.INVALID_SCALE)
                    playErrorSound(context.source)
                    return 0
                }
                HologramProperty.Scale(value)
            }

            "billboard" -> {
                val mode = try {
                    BillboardMode.valueOf(StringArgumentType.getString(context, "mode").uppercase())
                } catch (e: IllegalArgumentException) {
                    ErrorMessages.sendError(context.source, ErrorType.INVALID_BILLBOARD)
                    playErrorSound(context.source)
                    return 0
                }
                HologramProperty.BillboardMode(mode)
            }

            else -> return 0
        }

        DisplayHandler.updateDisplayProperty(name, property)
        playSuccessSound(context.source)
        when (display.displayType) {
            is DisplayData.DisplayType.Text -> TextEditMenu.show(context.source, name)
            is DisplayData.DisplayType.Item -> ItemEditMenu.show(context.source, name)
            is DisplayData.DisplayType.Block -> BlockEditMenu.show(context.source, name)
        }
        return 1
    }

    private fun executeTextProperty(context: CommandContext<ServerCommandSource>, property: String): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val updatedProperty = when (property) {
            "line" -> HologramProperty.Lines(listOf(StringArgumentType.getString(context, "content")))
            "lineWidth" -> HologramProperty.LineWidth(IntegerArgumentType.getInteger(context, "width"))
            "backgroundColor" -> {
                val color = StringArgumentType.getString(context, "color")
                if (!color.matches(Regex("^[0-9A-Fa-f]{6}$"))) {
                    ErrorMessages.sendError(context.source, ErrorType.INVALID_COLOR)
                    playErrorSound(context.source)
                    return 0
                }
                val opacity = IntegerArgumentType.getInteger(context, "opacity")
                val opacityHex = ((opacity.coerceIn(1, 100) / 100.0 * 255).toInt())
                    .toString(16)
                    .padStart(2, '0')
                    .uppercase()
                HologramProperty.Background("$opacityHex$color")
            }

            "textOpacity" -> HologramProperty.TextOpacity(IntegerArgumentType.getInteger(context, "opacity"))
            "shadow" -> HologramProperty.Shadow(BoolArgumentType.getBool(context, "enabled"))
            "seeThrough" -> HologramProperty.SeeThrough(BoolArgumentType.getBool(context, "enabled"))
            "alignment" -> {
                try {
                    HologramProperty.TextAlignment(
                        DisplayData.TextAlignment.valueOf(StringArgumentType.getString(context, "align").uppercase())
                    )
                } catch (e: IllegalArgumentException) {
                    ErrorMessages.sendError(context.source, ErrorType.INVALID_ALIGNMENT)
                    playErrorSound(context.source)
                    return 0
                }
            }

            else -> return 0
        }

        DisplayHandler.updateDisplayProperty(name, updatedProperty)
        playSuccessSound(context.source)
        TextEditMenu.show(context.source, name)
        return 1
    }

    private fun executeItemProperty(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val itemId = StringArgumentType.getString(context, "itemId")
        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val property = HologramProperty.ItemId(fullItemId)

        DisplayHandler.updateDisplayProperty(name, property)
        playSuccessSound(context.source)
        ItemEditMenu.show(context.source, name)
        return 1
    }

    private fun executeBlockProperty(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val blockId = StringArgumentType.getString(context, "blockId")
        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val property = HologramProperty.BlockId(fullBlockId)

        DisplayHandler.updateDisplayProperty(name, property)
        playSuccessSound(context.source)
        BlockEditMenu.show(context.source, name)
        return 1
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val rotation = HologramProperty.Rotation(
            HologramData.Rotation(
                FloatArgumentType.getFloat(context, "pitch"),
                FloatArgumentType.getFloat(context, "yaw")
            )
        )

        DisplayHandler.updateDisplayProperty(name, rotation)
        playSuccessSound(context.source)
        EditMenu.showDisplay(context.source, name)
        return 1
    }

    private fun executeResetCommonProperty(context: CommandContext<ServerCommandSource>, property: String): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val updatedProperty = when (property) {
            "scale" -> HologramProperty.Scale(null)
            "billboard" -> HologramProperty.BillboardMode(null)
            "rotation" -> HologramProperty.Rotation(null)
            else -> return 0
        }

        DisplayHandler.updateDisplayProperty(name, updatedProperty)
        playSuccessSound(context.source)
        EditMenu.showDisplay(context.source, name)
        return 1
    }

    private fun executeAddTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val content = StringArgumentType.getString(context, "content")
        val display = DisplayConfig.getDisplay(name)
        val textDisplay = display?.displayType as? DisplayData.DisplayType.Text ?: return 0

        val updatedLines = textDisplay.lines.toMutableList().apply {
            add(content)
        }

        val updatedProperty = HologramProperty.Lines(updatedLines.toList())
        DisplayHandler.updateDisplayProperty(name, updatedProperty)

        playSuccessSound(context.source)
        TextEditMenu.show(context.source, name)
        return 1
    }

    private fun executeEditTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")
        val content = StringArgumentType.getString(context, "content")

        val display = DisplayConfig.getDisplay(name)
        val textDisplay = display?.displayType as? DisplayData.DisplayType.Text ?: return 0

        if (lineIndex >= textDisplay.lines.size) return 0

        val updatedLines = textDisplay.lines.toMutableList().apply {
            set(lineIndex, content)
        }

        val updatedProperty = HologramProperty.Lines(updatedLines.toList())
        DisplayHandler.updateDisplayProperty(name, updatedProperty)

        playSuccessSound(context.source)
        TextEditMenu.show(context.source, name)
        return 1
    }

    private fun executeDeleteTextLine(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val lineIndex = IntegerArgumentType.getInteger(context, "lineIndex")

        val display = DisplayConfig.getDisplay(name)
        val textDisplay = display?.displayType as? DisplayData.DisplayType.Text ?: return 0

        if (lineIndex >= textDisplay.lines.size) return 0

        val updatedLines = textDisplay.lines.toMutableList().apply {
            removeAt(lineIndex)
        }

        val updatedProperty = HologramProperty.Lines(updatedLines.toList())
        DisplayHandler.updateDisplayProperty(name, updatedProperty)

        playSuccessSound(context.source)
        TextEditMenu.show(context.source, name)
        return 1
    }

    private fun executeOpenMenu(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayConfig.getDisplay(name)
        when (display?.displayType) {
            is DisplayData.DisplayType.Text -> TextEditMenu.show(context.source, name)
            is DisplayData.DisplayType.Item -> ItemEditMenu.show(context.source, name)
            is DisplayData.DisplayType.Block -> BlockEditMenu.show(context.source, name)
            null -> {
                ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
                playErrorSound(context.source)
                return 0
            }
        }
        playSuccessSound(context.source)
        return 1
    }

    private fun executeItemDisplayType(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val displayType = StringArgumentType.getString(context, "type")
        val validTypes = listOf(
            "none", "thirdperson_lefthand", "thirdperson_righthand",
            "firstperson_lefthand", "firstperson_righthand", "head",
            "gui", "ground", "fixed"
        )

        if (!validTypes.contains(displayType)) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_DISPLAY_TYPE)
            playErrorSound(context.source)
            return 0
        }

        val property = HologramProperty.ItemDisplayType(displayType)
        DisplayHandler.updateDisplayProperty(name, property)
        playSuccessSound(context.source)
        ItemEditMenu.show(context.source, name)
        return 1
    }

    private fun executeDefaultBackground(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        if (!DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayConfig.getDisplay(name)
        if (display?.displayType !is DisplayData.DisplayType.Text) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_DISPLAY_TYPE)
            playErrorSound(context.source)
            return 0
        }

        val updatedProperty = HologramProperty.Background(null)
        DisplayHandler.updateDisplayProperty(name, updatedProperty)
        playSuccessSound(context.source)
        TextEditMenu.show(context.source, name)
        return 1
    }
}