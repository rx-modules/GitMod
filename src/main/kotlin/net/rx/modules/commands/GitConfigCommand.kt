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
import net.minecraft.server.command.ServerCommandSource
import net.rx.modules.git.RawGitHandler
import net.rx.modules.config.ConfigManager
import java.util.concurrent.CompletableFuture

object GitConfigCommand : Command() {
    override fun register(dispatcher: Dispatcher) {
        val arg = CommandManager
            .argument("args", MessageArgumentType.message())
            .executes { editGitConfig(it, StringArgumentType.getString(it, "args")) }

        val gitConfigNode = CommandManager
            .literal("gitconfig")
            .requires { ConfigManager.isOperator(it.player!!.uuidAsString) }
            .then(arg)
            .build()

        dispatcher.root.addChild(gitConfigNode);
    }

    private fun editGitConfig(context: Context, args: String): Int {
        val pathToGitConfig = ConfigManager.dirPath
            .resolve("gitconfig")
            .resolve(context.source.player?.uuidAsString ?: "")
            .toAbsolutePath()

        val cmd = "git config -f \"$pathToGitConfig\" $args"

        GlobalScope.launch(Dispatchers.IO) {
            RawGitHandler.runGit(cmd, context.source)
        }

        return 1
    }

    internal object GitConfigSuggestionProvider : SuggestionProvider<Source> {
        override fun getSuggestions(
            context: Context, builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> {
            // Suggestions for common git sub-commands to run
            listOf(
                "user.name <name>",
                "user.email <name>@email.com",
                "remote.origin.url <username>:<token>@<git-repo>",
            ).map { builder.suggest(it) }

            return builder.buildFuture()
        }
    }
}