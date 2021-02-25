package net.rx.modules

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.rx.modules.GitConfig


// For support join https://discord.gg/v6v4pMv

class GitModKT : DedicatedServerModInitializer {
    fun init() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        println("Hello Fabric world!")
    }

    private var platform: FabricServerAudiences? = null
    private val adventure: FabricServerAudiences? = null

    fun adventure() {
        checkNotNull(adventure) { "Tried to access Adventure without a running server!" }
    }

    override fun onInitializeServer() {
        // This will ensure any platform data is cleared between game instances
        // This is important on the integrated server, where multiple server instances
        // can exist for one mod initialization.
        ServerLifecycleEvents.SERVER_STARTING.register(ServerStarting { server: MinecraftServer? ->
            this.platform = FabricServerAudiences.of(server!!)
            GitConfig.register(server!!)
            CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
                HomeCommand(dispatcher).register()
            })
        })
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerStopped {
            this.platform = null
        })
    }
}

