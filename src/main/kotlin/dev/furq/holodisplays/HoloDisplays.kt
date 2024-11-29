package dev.furq.holodisplays

import dev.furq.holodisplays.commands.MainCommand
import dev.furq.holodisplays.config.ConfigManager
import dev.furq.holodisplays.config.HologramConfig
import dev.furq.holodisplays.handlers.HologramHandler
import dev.furq.holodisplays.handlers.ViewerHandler
import dev.furq.holodisplays.utils.TextProcessor
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
        const val VERSION = "0.1.5"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        var SERVER: MinecraftServer? = null
            private set
    }

    override fun onInitialize() {
        try {
            registerServerEvents()
            initializeManagers()
            registerCommands()
            LOGGER.info("Initialized $MOD_ID v$VERSION")
        } catch (e: Exception) {
            LOGGER.error("Failed to initialize $MOD_ID!", e)
        }
    }

    private fun registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register { SERVER = it }
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (server.playerManager.playerList.isNotEmpty() && HologramConfig.getHolograms().isNotEmpty()) {
                server.playerManager.playerList.forEach { player ->
                    ViewerHandler.updatePlayerVisibility(player)
                }
                TextProcessor.updateAnimations()
            }
            TextProcessor.tick()
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ViewerHandler.updatePlayerVisibility(handler.player)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ViewerHandler.clearViewers(handler.player)
        }
    }

    private fun initializeManagers() {
        val configDir = FabricLoader.getInstance().configDir.resolve(MOD_ID)
        ConfigManager.init(configDir)
        HologramHandler.init()
        TextProcessor.init()
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            try {
                MainCommand.register(dispatcher)
            } catch (e: Exception) {
                LOGGER.error("Failed to register commands!", e)
            }
        }
    }
}