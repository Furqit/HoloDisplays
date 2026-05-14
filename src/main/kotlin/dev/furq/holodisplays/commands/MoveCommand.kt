package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.world.phys.Vec3

object MoveCommand {
    fun register(): ArgumentBuilder<CommandSourceStack, *> = Commands.literal("move")
        .then(Commands.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeMove(context) }
            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                        .executes { context -> executeMove(context) })))
        )

    private fun executeMove(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pos = try {
            Vec3(
                DoubleArgumentType.getDouble(context, "x"),
                DoubleArgumentType.getDouble(context, "y"),
                DoubleArgumentType.getDouble(context, "z")
            )
        } catch (_: IllegalArgumentException) {
            context.source.position
        }
        return requirePlayer(context)?.let { player ->
            //~ if >=1.21.11 'location' -> 'identifier'
            HologramManager.updatePosition(name, pos, context.source.level.dimension().identifier().toString(), context.source)
            HologramEdit.open(player, name)
            1
        } ?: 0
    }
}