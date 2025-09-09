package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.Vec3d

object MoveCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("move")
        .then(CommandManager.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeMove(context) }
            .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                    .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                        .executes { context -> executeMove(context) })))
        )

    private fun executeMove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pos = try {
            Vec3d(
                DoubleArgumentType.getDouble(context, "x"),
                DoubleArgumentType.getDouble(context, "y"),
                DoubleArgumentType.getDouble(context, "z")
            )
        } catch (_: IllegalArgumentException) {
            context.source.position
        }
        return requirePlayer(context)?.let { player ->
            HologramManager.updatePosition(name, pos, context.source.world.registryKey.value.toString(), context.source)
            HologramEdit.open(player, name)
            1
        } ?: 0
    }
}