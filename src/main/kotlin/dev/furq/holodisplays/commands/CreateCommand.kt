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
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CreateCommand {

    fun registerHologram(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("create")
        .then(buildHologramCommand())

    fun registerDisplay(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("create")
        .then(buildDisplayCommand())

    private fun buildHologramCommand(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.argument("name", StringArgumentType.word())
            .executes { context -> executeHologram(context) }
    }

    private fun buildDisplayCommand(): ArgumentBuilder<CommandSourceStack, *> {
        return Commands.argument("name", StringArgumentType.word())
            .then(Commands.literal("text")
                .then(Commands.argument("content", StringArgumentType.string())
                    .executes { context -> executeText(context, null) }
                    .then(Commands.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeText(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(Commands.literal("item")
                .then(Commands.argument("itemId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestItemIds(builder) }
                    .executes { context -> executeItem(context, null) }
                    .then(Commands.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeItem(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(Commands.literal("block")
                .then(Commands.argument("blockId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestBlockIds(builder) }
                    .executes { context -> executeBlock(context, null) }
                    .then(Commands.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeBlock(context, StringArgumentType.getString(context, "hologramName")) })))
            .then(Commands.literal("entity")
                .then(Commands.argument("entityId", StringArgumentType.string())
                    .suggests { _, builder -> CommandUtils.suggestEntityIds(builder) }
                    .executes { context -> executeEntity(context, null) }
                    .then(Commands.argument("hologramName", StringArgumentType.word())
                        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
                        .executes { context -> executeEntity(context, StringArgumentType.getString(context, "hologramName")) })))
    }

    private fun executeHologram(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            HologramManager.createHologram(name, player)
            HologramEdit.open(player, name)
            1
        } ?: 0
    }

    private fun executeText(context: CommandContext<CommandSourceStack>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val content = StringArgumentType.getString(context, "content")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createTextDisplay(name, content, context.source)
        }
    }

    private fun executeItem(context: CommandContext<CommandSourceStack>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val itemId = StringArgumentType.getString(context, "itemId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createItemDisplay(name, itemId, context.source)
        }
    }

    private fun executeBlock(context: CommandContext<CommandSourceStack>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val blockId = StringArgumentType.getString(context, "blockId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createBlockDisplay(name, blockId, context.source)
        }
    }

    private fun executeEntity(context: CommandContext<CommandSourceStack>, hologramName: String?): Int {
        val name = StringArgumentType.getString(context, "name")
        val entityId = StringArgumentType.getString(context, "entityId")
        return createDisplay(context, hologramName, name) {
            DisplayManager.createEntityDisplay(name, entityId, context.source)
        }
    }

    private fun createDisplay(
        context: CommandContext<CommandSourceStack>,
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