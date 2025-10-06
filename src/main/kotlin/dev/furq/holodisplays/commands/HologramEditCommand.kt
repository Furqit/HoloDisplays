package dev.furq.holodisplays.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.furq.holodisplays.gui.HologramEdit
import dev.furq.holodisplays.managers.HologramManager
import dev.furq.holodisplays.utils.CommandUtils
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector3f

object HologramEditCommand : EditCommand() {
    override fun updateScale(name: String, scale: Vector3f, source: ServerCommandSource) = HologramManager.updateScale(name, scale, source)
    override fun resetScale(name: String, source: ServerCommandSource) = HologramManager.updateScale(name, null, source)
    override fun updateBillboard(name: String, mode: String, source: ServerCommandSource) = HologramManager.updateBillboard(name, mode, source)
    override fun resetBillboard(name: String, source: ServerCommandSource) = HologramManager.updateBillboard(name, null, source)
    override fun updateRotation(name: String, pitch: Float, yaw: Float, roll: Float, source: ServerCommandSource) = HologramManager.updateRotation(name, pitch, yaw, roll, source)
    override fun resetRotation(name: String, source: ServerCommandSource) = HologramManager.updateRotation(name, null, null, null, source)
    override fun updateCondition(name: String, condition: String?, source: ServerCommandSource) = HologramManager.updateCondition(name, condition, source)
    override fun openEditGui(player: net.minecraft.server.network.ServerPlayerEntity, name: String) = HologramEdit.open(player, name)

    fun register(): ArgumentBuilder<ServerCommandSource, *> = CommandManager
        .literal("edit")
        .then(CommandManager.argument("name", StringArgumentType.word())
            .suggests { _, builder -> CommandUtils.suggestHolograms(builder) }
            .executes { context -> executeEdit(context) }
            .then(buildScaleCommands())
            .then(buildBillboardCommands())
            .then(buildUpdateRateCommands())
            .then(buildViewRangeCommands())
            .then(buildRotationCommands())
            .then(buildConditionCommands())
        )

    private fun buildUpdateRateCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("updateRate")
            .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1))
                .executes { context -> executeUpdateRate(context) })
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetUpdateRate(context) })
    }

    private fun buildViewRangeCommands(): ArgumentBuilder<ServerCommandSource, *> {
        return CommandManager.literal("viewRange")
            .then(CommandManager.argument("blocks", FloatArgumentType.floatArg(1f, 128f))
                .executes { context -> executeViewRange(context) })
            .then(CommandManager.literal("reset")
                .executes { context -> executeResetViewRange(context) })
    }

    private fun executeUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val ticks = IntegerArgumentType.getInteger(context, "ticks")
        HologramManager.updateUpdateRate(name, ticks, context.source)
        return 1
    }

    private fun executeResetUpdateRate(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        HologramManager.updateUpdateRate(name, null, context.source)
        return 1
    }

    private fun executeViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        val blocks = FloatArgumentType.getFloat(context, "blocks")
        HologramManager.updateViewRange(name, blocks, context.source)
        return 1
    }

    private fun executeResetViewRange(context: CommandContext<ServerCommandSource>): Int {
        val name = StringArgumentType.getString(context, "name")
        HologramManager.updateViewRange(name, null, context.source)
        return 1
    }
}