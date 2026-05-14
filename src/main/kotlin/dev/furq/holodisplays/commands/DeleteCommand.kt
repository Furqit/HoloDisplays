package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.DeleteConfirmation
import dev.furq.holodisplays.gui.MainMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object DeleteCommand {
    fun registerDisplay(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("delete")
        .then(Commands.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
            .executes { context -> executeDisplay(context) })

    fun registerHologram(): ArgumentBuilder<CommandSourceStack, *> = Commands
        .literal("delete")
        .then(Commands.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeHologram(context) })

    private fun executeHologram(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            DeleteConfirmation.open(player, name, "hologram") {
                MainMenu.openMainMenu(player)
            }
            1
        } ?: 0
    }

    private fun executeDisplay(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        return requirePlayer(context)?.let { player ->
            DeleteConfirmation.open(player, name, "display") {
                MainMenu.openMainMenu(player)
            }
            1
        } ?: 0
    }
}