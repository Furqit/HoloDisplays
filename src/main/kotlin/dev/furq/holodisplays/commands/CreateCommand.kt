package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DisplayEdit
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object CreateCommand {
    private val displayManager = DisplayManager()
    private val hologramManager = HologramManager()

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("create")
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
        val player = context.source.player ?: return 0
        hologramManager.createHologram(name, player)
        HologramEdit.open(player, name)
        return 1
    }

    private fun executeText(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")

        displayManager.createTextDisplay(name, content, context.source)
        if (hologramName != null) {
            hologramManager.addDisplayToHologram(hologramName, name, context.source)
            HologramEdit.open(context.source.player!!, hologramName)
        } else {
            DisplayEdit.open(context.source.player!!, name)
        }
        return 1
    }

    private fun executeItem(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")

        displayManager.createItemDisplay(name, itemId, context.source)
        if (hologramName != null) {
            hologramManager.addDisplayToHologram(hologramName, name, context.source)
            HologramEdit.open(context.source.player!!, hologramName)
        } else {
            DisplayEdit.open(context.source.player!!, name)
        }
        return 1
    }

    private fun executeBlock(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")

        displayManager.createBlockDisplay(name, blockId, context.source)
        if (hologramName != null) {
            hologramManager.addDisplayToHologram(hologramName, name, context.source)
            HologramEdit.open(context.source.player!!, hologramName)
        } else {
            DisplayEdit.open(context.source.player!!, name)
        }
        return 1
    }
}