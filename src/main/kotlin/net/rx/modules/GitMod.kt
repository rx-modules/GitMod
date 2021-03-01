package net.rx.modules

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.rx.modules.commands.Command
import net.rx.modules.commands.Dispatcher
import net.rx.modules.config.ConfigManager


// For support join https://discord.gg/v6v4pMv

object GitMod : ModInitializer {
    //private var platform: FabricServerAudiences? = null
    //private val adventure: FabricServerAudiences? = null

//    fun adventure() {
//        checkNotNull(adventure) { "Tried to access Adventure without a running server!" }
//    }

    override fun onInitialize() {
        // This will ensure any platform data is cleared between game instances
        // This is important on the integrated server, where multiple server instances
        // can exist for one mod initialization.
        ServerLifecycleEvents.SERVER_STARTING.register {
            //this.platform = FabricServerAudiences.of(it)

            ConfigManager.register(it)
        }

        ServerLifecycleEvents.SERVER_STOPPED.register {
            //this.platform = null
        }

        CommandRegistrationCallback.EVENT.register(::registerCommands)
    }

    private fun registerCommands(dispatcher: Dispatcher, dedicated: Boolean) {
        logger.info("Registering commands")
        Command.registerAll(dispatcher)
    }
}

