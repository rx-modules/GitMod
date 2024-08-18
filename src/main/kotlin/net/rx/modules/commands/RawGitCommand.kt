package net.rx.modules.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.MessageArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.ServerCommandSource
import net.rx.modules.config.ConfigManager
import net.rx.modules.git.RawGitHandler
import java.util.concurrent.CompletableFuture


object RawGitCommand : Command() {

    override fun register(dispatcher: Dispatcher) {
        val arg = CommandManager
            .argument("target", StringArgumentType.greedyString())
            .executes { gitCommand(it, StringArgumentType.getString(it, "target")) }

        val gitNode = CommandManager
            .literal("git")
            .requires { ConfigManager.isOperator(it.player!!.uuidAsString) }
            .then(arg)
            .build()

        dispatcher.root.addChild(gitNode);
    }

    private fun gitCommand(context: Context, args: String): Int {
        print(args)
        if (RawGitHandler.executing) {
            val feedback = "${RawGitHandler.executor} is current running ${RawGitHandler.command}. Please wait.."
            context.source.sendFeedback({ red(feedback) }, true)
            return 0
        }

        val path = ConfigManager.getGitPath()

        GlobalScope.launch(Dispatchers.IO) {
            RawGitHandler.runGit(path, args, context.source)
        }

        return 1
    }

    internal object GitSuggestionProvider : SuggestionProvider<Source> {
        override fun getSuggestions(
            context: Context, builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            // Suggestions for common git sub-commands to run
            listOf(
                "status",
                "log",
                "add .",
                "add -A",
                "commit -m",
                "commit -a -m",
                "push origin master",
                "pull origin master"
            ).map { builder.suggest(it) }

            return builder.buildFuture()
        }
    }
}
