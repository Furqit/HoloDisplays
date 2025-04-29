package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object MoveCommand {
    private val hologramManager = HologramManager()

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("move")
        .then(CommandManager.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeMove(context) }
            .then(
                CommandManager.argument("x", FloatArgumentType.floatArg())
                    .then(
                        CommandManager.argument("y", FloatArgumentType.floatArg())
                            .then(CommandManager.argument("z", FloatArgumentType.floatArg())
                                .executes { context -> executeMove(context) })
                    )
            )
        )

    private fun executeMove(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val pos = try {
            Vector3f(
                FloatArgumentType.getFloat(context, "x"),
                FloatArgumentType.getFloat(context, "y"),
                FloatArgumentType.getFloat(context, "z")
            )
        } catch (e: IllegalArgumentException) {
            context.source.position.toVector3f()
        }

        hologramManager.updatePosition(name, pos, context.source.world.registryKey.value.toString(), context.source)
        HologramEdit.open(context.source.playerOrThrow, name)
        return 1
    }
}