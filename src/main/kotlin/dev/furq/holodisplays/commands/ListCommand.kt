package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.menu.ListMenu
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object ListCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("list")
        .then(
            CommandManager.literal("hologram")
                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                    .executes { context -> executeHolograms(context) }
                )
                .executes { context -> executeHolograms(context, 1) }
        )
        .then(CommandManager.literal("display")
            .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                .executes { context -> executeDisplays(context) }
            )
            .executes { context -> executeDisplays(context, 1) }
        )

    private fun executeHolograms(context: CommandContext<ServerCommandSource>, defaultPage: Int = 0): Int {
        val page = if (defaultPage == 0) {
            IntegerArgumentType.getInteger(context, "page")
        } else defaultPage

        val holograms = HologramConfig.getHolograms()
        ListMenu.show(
            context.source,
            "Holograms",
            holograms.keys.toList(),
            page,
            { name -> ListMenu.showHologramEntry(context.source, name) },
            "/holo list hologram "
        )
        return 1
    }

    private fun executeDisplays(context: CommandContext<ServerCommandSource>, defaultPage: Int = 0): Int {
        val page = if (defaultPage == 0) {
            IntegerArgumentType.getInteger(context, "page")
        } else defaultPage

        val displays = DisplayConfig.getDisplays()
        ListMenu.show(
            context.source,
            "Displays",
            displays.keys.toList(),
            page,
            { displayId -> ListMenu.showDisplayEntry(context.source, displayId) },
            "/holo list display "
        )
        return 1
    }
}