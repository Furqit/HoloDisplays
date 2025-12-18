package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.gui.NearList
import dev.furq.holodisplays.managers.FeedbackManager
import dev.furq.holodisplays.utils.CommandUtils.requirePlayer
import dev.furq.holodisplays.utils.FeedbackType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import kotlin.math.sqrt

object NearCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager.literal("near")
        .executes { context -> execute(context, 10) }
        .then(CommandManager.argument("radius", IntegerArgumentType.integer(1))
                .executes { context -> execute(context, IntegerArgumentType.getInteger(context, "radius")) }
        )

    private fun execute(context: CommandContext<ServerCommandSource>, radius: Int): Int {
        return requirePlayer(context)?.let { player ->
            val world = player.world.registryKey.value.toString()
            val playerPos = player.pos
            val radiusSq = radius * radius

            val nearbyHolograms = HologramConfig.getHolograms().asSequence()
                .filter { (_, data) -> data.position.world == world }
                .mapNotNull { (name, data) ->
                    val hologramPos = data.position.toVec3f()
                    val distSq = playerPos.squaredDistanceTo(
                        hologramPos.x.toDouble(),
                        hologramPos.y.toDouble(),
                        hologramPos.z.toDouble()
                    )
                    if (distSq <= radiusSq) name to sqrt(distSq) else null
                }
                .sortedBy { it.second }
                .toList()

            if (nearbyHolograms.isEmpty()) {
                FeedbackManager.send(context.source, FeedbackType.NO_HOLOGRAMS_NEAR, "radius" to radius)
            } else {
                NearList.open(player, nearbyHolograms)
            }
            1
        } ?: 0
    }
}
