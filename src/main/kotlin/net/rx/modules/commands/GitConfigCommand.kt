package net.rx.modules.commands

import com.github.p03w.aegis.AegisCommandBuilder
import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.rx.modules.GitHandler
import net.rx.modules.config.ConfigManager

object GitConfigCommand : Command() {
    override fun register(dispatcher: Dispatcher) {
        dispatcher.register(
            AegisCommandBuilder("gitconfig") {
                requires { it.hasPermissionLevel(4) }

                literal("edit") {
                    greedyString("args") {
                        executes {
                           editGitConfig(it, StringArgumentType.getString(it, "args"))
                        }
                    }
                }
            }.build()
        )
    }

    private fun editGitConfig(context: Context, args: String): Int {
        val pathToGitConfig = ConfigManager.dirPath
            .resolve("gitconfig")
            .resolve("${context.source.player.uuidAsString}")
            .toAbsolutePath()

        val cmd = "git config -f $pathToGitConfig $args"

        GlobalScope.launch(Dispatchers.IO) {
            GitHandler.runGit(cmd, context.source)
        }

        return 1
    }
}