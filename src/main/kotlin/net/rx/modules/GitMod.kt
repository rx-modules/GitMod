package net.rx.modules

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.rx.modules.commands.Command
import net.rx.modules.commands.Dispatcher
import net.rx.modules.config.ConfigManager


// For support join https://discord.gg/v6v4pMv

object GitMod : ModInitializer {

    override fun onInitialize() {
        // This will ensure any platform data is cleared between game instances
        // This is important on the integrated server, where multiple server instances
        // can exist for one mod initialization.
        ServerLifecycleEvents.SERVER_STARTING.register {
            ConfigManager.register(it)
        }

//        ServerLifecycleEvents.SERVER_STOPPED.register {
//
//        }

        CommandRegistrationCallback.EVENT.register(::registerCommands)
    }

    private fun registerCommands(
        dispatcher: Dispatcher,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        logger.info("Registering commands")
        Command.registerAll(dispatcher)
    }
}