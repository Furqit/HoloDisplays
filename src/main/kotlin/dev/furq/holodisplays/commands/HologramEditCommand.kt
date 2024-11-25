package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.data.HologramData
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.menu.EditMenu
import dev.furq.holodisplays.utils.CommandUtils
import dev.furq.holodisplays.utils.CommandUtils.playErrorSound
import dev.furq.holodisplays.utils.CommandUtils.playSuccessSound
import dev.furq.holodisplays.utils.ErrorMessages
import dev.furq.holodisplays.utils.ErrorMessages.ErrorType
import dev.furq.holodisplays.utils.HandlerUtils.HologramProperty
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object HologramEditCommand {
    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .argument("name", StringArgumentType.word())
        .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
        .executes { context -> executeOpenMenu(context) }
        .then(
            CommandManager.literal("scale")
                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.1f))
                    .executes { context -> executeScale(context) })
        )
        .then(CommandManager.literal("billboard")
            .then(CommandManager.argument("mode", StringArgumentType.word())
                .suggests { _, builder ->
                    BillboardMode.entries.forEach { builder.suggest(it.name.lowercase()) }
                    builder.buildFuture()
                }
                .executes { context -> executeBillboard(context) })
        )
        .then(
            CommandManager.literal("updateRate")
                .then(CommandManager.argument("ticks", IntegerArgumentType.integer(20))
                    .executes { context -> executeUpdateRate(context) })
        )
        .then(
            CommandManager.literal("viewRange")
                .then(CommandManager.argument("blocks", FloatArgumentType.floatArg(1f, 128f))
                    .executes { context -> executeViewRange(context) })
        )
        .then(
            CommandManager.literal("rotation")
                .then(
                    CommandManager.argument("pitch", FloatArgumentType.floatArg(-180f, 180f))
                        .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                            .executes { context -> executeRotation(context) })
                )
        )

    private fun executeOpenMenu(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        EditMenu.showHologram(context.source, name)
        playSuccessSound(context.source)
        return 1
    }

    private fun executeScale(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val value = FloatArgumentType.getFloat(context, "value")
        if (value < 0.1f) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_SCALE)
            playErrorSound(context.source)
            return 0
        }

        HologramHandler.updateHologramProperty(name, HologramProperty.Scale(value))
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }

    private fun executeBillboard(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val modeStr = StringArgumentType.getString(context, "mode").uppercase()
        if (!BillboardMode.entries.map { it.name }.contains(modeStr)) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_BILLBOARD)
            playErrorSound(context.source)
            return 0
        }

        val mode = BillboardMode.valueOf(modeStr)
        HologramHandler.updateHologramProperty(name, HologramProperty.BillboardMode(mode))
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }

    private fun executeUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val ticks = IntegerArgumentType.getInteger(context, "ticks")
        HologramHandler.updateHologramProperty(name, HologramProperty.UpdateRate(ticks))
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }

    private fun executeViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val blocks = FloatArgumentType.getFloat(context, "blocks")
        if (blocks !in 1f..128f) {
            ErrorMessages.sendError(context.source, ErrorType.INVALID_VIEW_RANGE)
            playErrorSound(context.source)
            return 0
        }

        HologramHandler.updateHologramProperty(name, HologramProperty.ViewRange(blocks.toDouble()))
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }

    private fun executeRotation(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")

        if (!HologramConfig.exists(name)) {
            ErrorMessages.sendError(context.source, ErrorType.HOLOGRAM_NOT_FOUND)
            playErrorSound(context.source)
            return 0
        }

        val pitch = FloatArgumentType.getFloat(context, "pitch")
        val yaw = FloatArgumentType.getFloat(context, "yaw")

        HologramHandler.updateHologramProperty(
            name,
            HologramProperty.Rotation(HologramData.Rotation(pitch, yaw))
        )
        playSuccessSound(context.source)
        EditMenu.showHologram(context.source, name)
        return 1
    }
}