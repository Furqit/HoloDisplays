package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DisplayEdit
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.DisplayManager
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object CreateCommand {

    fun registerHologram(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("create")
        .then(buildHologramCommand())

    fun registerDisplay(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("create")
        .then(buildDisplayCommand())

    private fun buildHologramCommand(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.argument("name", StringArgumentType.word())
            .executes { context -> executeHologram(context) }
    }

    private fun buildDisplayCommand(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.argument("name", StringArgumentType.word())
            .then(CommandManager.literal("text")
                .then(CommandManager.argument("content", StringArgumentType.string())
                    .executes { context -> executeText(context, null) }
                    .then(CommandManager.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeText(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(CommandManager.literal("item")
                .then(CommandManager.argument("itemId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                    .executes { context -> executeItem(context, null) }
                    .then(CommandManager.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeItem(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(CommandManager.literal("block")
                .then(CommandManager.argument("blockId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                    .executes { context -> executeBlock(context, null) }
                    .then(CommandManager.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeBlock(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(CommandManager.literal("entity")
                .then(CommandManager.argument("entityId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestEntityIds(builder) }
                    .executes { context -> executeEntity(context, null) }
                    .then(CommandManager.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeEntity(context, StringArgumentType.getString(context, "hologramName")) })))
    }

    private fun executeHologram(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            HologramManager.createHologram(name, player)
            HologramEdit.open(player, name)
            1
        } ?: 0
    }

    private fun executeText(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createTextDisplay(name, content, context.source)
        }
    }

    private fun executeItem(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createItemDisplay(name, itemId, context.source)
        }
    }

    private fun executeBlock(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createBlockDisplay(name, blockId, context.source)
        }
    }

    private fun executeEntity(context: CommandContext<ServerCommandSource>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val entityId = StringArgumentType.getString(context, "entityId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createEntityDisplay(name, entityId, context.source)
        }
    }

    private fun createDisplay(
        context: CommandContext<ServerCommandSource>,
        hologramName: String?,
        displayName: String,
        createAction: () -> Unit
    ): Int = requirePlayer(context)?.let { player ->
        createAction()
        if (hologramName != null) {
            HologramManager.addDisplayToHologram(hologramName, displayName, context.source)
            HologramEdit.open(player, hologramName)
        } else {
            DisplayEdit.open(player, displayName)
        }
        1
    } ?: 0
}