package dev.furq.holodisplays

import dev.furq.holodisplays.api.HoloDisplaysAPIInternal
import dev.furq.holodisplays.commands.MainCommand
import dev.furq.holodisplays.config.ConfigManager
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.handlers.ErrorHandler.safeCall
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.PacketHandler
import dev.furq.holodisplays.handlers.TickHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HoloDisplays : ModInitializer {
    companion object {
        const val MOD_ID = "HoloDisplays"
        const val VERSION = "0.4.8"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        var SERVER: MinecraftServer? = null
            private set
    }

    override fun onInitialize() {
        registerServerEvents()
        initializeHandlers()
        registerCommands()
        LOGGER.info("Initialized $MOD_ID v$VERSION")
    }

    private fun handleServerTick(server: MinecraftServer) {
        val players = server.playerList.players
        if (players.isNotEmpty() && (HologramConfig.getHolograms().isNotEmpty() || HoloDisplaysAPIInternal.hasApiHologramsUnchecked())) {
            TickHandler.tick(players)
            players.forEach { player ->
                ViewerHandler.updatePlayerVisibility(player)
            }
        }
    }

    private fun registerServerEvents() = safeCall {
        ServerLifecycleEvents.SERVER_STARTING.register { SERVER = it }
        ServerTickEvents.END_SERVER_TICK.register { server ->
            handleServerTick(server)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ViewerHandler.updatePlayerVisibility(handler.player)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ViewerHandler.clearViewers(handler.player)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            HoloDisplaysAPIInternal.clearAll()
            HologramHandler.clearWorldCache()
            TickHandler.init()
            PacketHandler.resetEntityTracking()
        }
    }

    private fun initializeHandlers() = safeCall {
        val configDir = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        ConfigManager.init(configDir)
        HologramHandler.init()
        TickHandler.init()
    }

    private fun registerCommands() = safeCall {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            MainCommand.register(dispatcher)
        }
    }
}