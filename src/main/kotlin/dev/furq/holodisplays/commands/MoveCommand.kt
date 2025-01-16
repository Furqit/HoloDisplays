package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.Utils
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object MoveCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("move")
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
        val worldId = context.source.world.registryKey.value.toString()
        val pos = try {
            Vector3f(
                FloatArgumentType.getFloat(context, "x"),
                FloatArgumentType.getFloat(context, "y"),
                FloatArgumentType.getFloat(context, "z")
            )
        } catch (e: IllegalArgumentException) {
            context.source.position.toVector3f()
        }

        if (Utils.updateHologramPosition(name, pos, worldId, context.source)) {
            HologramEdit.open(context.source.playerOrThrow, name)
            return 1
        }
        return 0
    }
}