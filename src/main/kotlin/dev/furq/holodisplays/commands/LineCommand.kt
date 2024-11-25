package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.DisplayConfig
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.DisplayData
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.menu.EditMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object LineCommand {
    fun register(): LiteralArgumentBuilder<ServerCommandSource> = CommandManager.literal("line")
        .then(CommandManager.argument("hologram", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("display", StringArgumentType.word())
                    .suggests { _, builder -> CommandUtils.suggestDisplays(builder) }
                    .executes { context -> executeAdd(context) }
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("index", IntegerArgumentType.integer())
                    .executes { context -> executeRemove(context) }
                )
            )
        )

    private fun executeAdd(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val displayId = StringArgumentType.getString(context, "display")

        if (!HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val display = DisplayConfig.getDisplay(displayId) ?: run {
            ErrorMessages.sendError(context.source, ErrorType.DISPLAY_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val line = when (display.displayType) {
            is DisplayData.DisplayType.Text -> HologramData.DisplayLine(text = displayId)
            is DisplayData.DisplayType.Item -> HologramData.DisplayLine(item = displayId)
            is DisplayData.DisplayType.Block -> HologramData.DisplayLine(block = displayId)
        }

        HologramHandler.addLine(hologramName, line)
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, hologramName)
        return 1
    }

    private fun executeRemove(context: CommandContext<ServerCommandSource>): Int {
        val hologramName = StringArgumentType.getString(context, "hologram")
        val index = IntegerArgumentType.getInteger(context, "index")

        if (!HologramConfig.exists(hologramName)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val hologram = HologramConfig.getHologram(hologramName) ?: return 0
        if (index >= hologram.displays.size) {
            ErrorMessages.sendError(context.source, ErrorType.LINE_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        HologramHandler.removeLine(hologramName, index)
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, hologramName)
        return 1
    }
}