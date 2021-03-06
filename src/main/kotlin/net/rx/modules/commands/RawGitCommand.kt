package net.rx.modules.commands

import com.github.p03w.aegis.AegisCommandBuilder
import com.github.p03w.aegis.aegisCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.rx.modules.git.RawGitHandler
import net.rx.modules.config.ConfigManager
import java.util.concurrent.CompletableFuture


object RawGitCommand : Command() {

    override fun register(dispatcher: Dispatcher) {
        dispatcher.register(
            aegisCommand("rawgit") {
                requires { ConfigManager.isOperator(it.player.uuidAsString) }

                // executes { invalidCommand(it, "Invalid invocation. Try /git status") }

                greedyString("args") {
                    executes { gitCommand(it, StringArgumentType.getString(it, "args")) }
                    suggests(GitSuggestionProvider::getSuggestions)
                }
            }
        )
    }

    private fun gitCommand(context: Context, args: String): Int {
        if (RawGitHandler.executing) {
            val feedback = "${RawGitHandler.executor} is current running ${RawGitHandler.command}. Please wait.."
            context.source.sendFeedback(red(feedback), true)
            return 0
        }

        val path = ConfigManager.getGitPath()

        GlobalScope.launch(Dispatchers.IO) {
            RawGitHandler.runGit(path, args, context.source)
        }

        return 0
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
