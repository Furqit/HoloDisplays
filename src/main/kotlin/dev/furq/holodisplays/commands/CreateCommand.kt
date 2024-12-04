package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.data.common.Position
import dev.furq.holodisplays.data.common.Rotation
import dev.furq.holodisplays.data.common.Scale
import dev.furq.holodisplays.data.display.BlockDisplay
import dev.furq.holodisplays.data.display.ItemDisplay
import dev.furq.holodisplays.data.display.TextDisplay
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.menu.BlockEditMenu
import dev.furq.holodisplays.menu.EditMenu
import dev.furq.holodisplays.menu.ItemEditMenu
import dev.furq.holodisplays.menu.TextEditMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier

object CreateCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("create")
        .then(
            CommandManager.literal("hologram")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes { context -> executeHologram(context) })
        )
        .then(CommandManager.literal("display")
            .then(CommandManager.literal("text")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .then(CommandManager.argument("content", StringArgumentType.greedyString())
                        .executes { context -> executeText(context, null) }
                    )
                )
            )
            .then(CommandManager.literal("item")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .then(CommandManager.argument("itemId", StringArgumentType.greedyString())
                        .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                        .executes { context -> executeItem(context, null) }
                    )
                )
            )
            .then(CommandManager.literal("block")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .then(CommandManager.argument("blockId", StringArgumentType.greedyString())
                        .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                        .executes { context -> executeBlock(context, null) }
                    )
                )
            )
            .then(CommandManager.argument("hologramName", StringArgumentType.word())
                .then(CommandManager.literal("text")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.argument("content", StringArgumentType.greedyString())
                            .executes { context ->
                                executeText(
                                    context,
                                    StringArgumentType.getString(context, "hologramName")
                                )
                            }
                        )
                    )
                )
                .then(CommandManager.literal("item")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.argument("itemId", StringArgumentType.greedyString())
                            .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                            .executes { context ->
                                executeItem(
                                    context,
                                    StringArgumentType.getString(context, "hologramName")
                                )
                            }
                        )
                    )
                )
                .then(CommandManager.literal("block")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .then(CommandManager.argument("blockId", StringArgumentType.greedyString())
                            .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                            .executes { context ->
                                executeBlock(
                                    context,
                                    StringArgumentType.getString(context, "hologramName")
                                )
                            }
                        )
                    )
                )
            )
        )

    private fun executeHologram(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_EXISTS)
            playErrorSound(context.source)
            return 0
        }

        val defaultDisplayName = "${name}_text"
        val defaultDisplay = DisplayData(
            display = TextDisplay(
                lines = mutableListOf("<gr #ffffff #008000>Hello, %player:name%</gr>")
            )
        )

        DisplayConfig.saveDisplay(defaultDisplayName, defaultDisplay)

        val pos = context.source.position
        val worldId = context.source.world.registryKey.value.toString()
        val hologram = HologramData(
            displays = mutableListOf(HologramData.DisplayLine(defaultDisplayName)),
            position = Position(
                worldId,
                String.format("%.3f", pos.x).toFloat(),
                String.format("%.3f", pos.y).toFloat(),
                String.format("%.3f", pos.z).toFloat()
            ),
            rotation = Rotation(),
            scale = Scale(),
            billboardMode = BillboardMode.CENTER,
            updateRate = 20,
            viewRange = 16.0,
        )

        HologramHandler.createHologram(name, hologram)
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }

    private fun executeText(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")

        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(context.source)
            return 0
        }

        if (hologramName != null && !HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayData(
            display = TextDisplay(
                lines = mutableListOf(content)
            )
        )

        DisplayConfig.saveDisplay(name, display)

        if (hologramName != null) {
            HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.AddLine(name))
            EditMenu.showHologram(context.source, hologramName)
        } else {
            TextEditMenu.show(context.source, name)
        }

        playSuccessSound(context.source)
        return 1
    }

    private fun executeItem(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")

        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(context.source)
            return 0
        }

        if (hologramName != null && !HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val fullItemId = if (!itemId.contains(":")) "minecraft:$itemId" else itemId
        val itemIdentifier = Identifier.tryParse(fullItemId)

        if (itemIdentifier == null || !Registries.ITEM.containsId(itemIdentifier)) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_ITEM)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayData(
            display = ItemDisplay(
                id = fullItemId
            )
        )

        DisplayConfig.saveDisplay(name, display)

        if (hologramName != null) {
            HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.AddLine(name))
            EditMenu.showHologram(context.source, hologramName)
        } else {
            ItemEditMenu.show(context.source, name)
        }

        playSuccessSound(context.source)
        return 1
    }

    private fun executeBlock(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")

        if (DisplayConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_EXISTS)
            playErrorSound(context.source)
            return 0
        }

        if (hologramName != null && !HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val fullBlockId = if (!blockId.contains(":")) "minecraft:$blockId" else blockId
        val blockIdentifier = Identifier.tryParse(fullBlockId)

        if (blockIdentifier == null || !Registries.BLOCK.containsId(blockIdentifier)) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_BLOCK)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayData(
            display = BlockDisplay(
                id = fullBlockId
            )
        )

        DisplayConfig.saveDisplay(name, display)

        if (hologramName != null) {
            HologramHandler.updateHologramProperty(hologramName, HologramHandler.HologramProperty.AddLine(name))
            EditMenu.showHologram(context.source, hologramName)
        } else {
            BlockEditMenu.show(context.source, name)
        }

        playSuccessSound(context.source)
        return 1
    }
}