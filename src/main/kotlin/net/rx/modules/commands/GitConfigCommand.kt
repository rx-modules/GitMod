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
                requires(Permissions::checkOperatorPermission)

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
        val configPath = "${context.source.player.uuidAsString}.gitconfig"
        val cmd = "git -C ${ConfigManager.dirPath.resolve("gitconfigs")} -f $configPath $args"

        GlobalScope.launch(Dispatchers.IO) {
            GitHandler.runGit(cmd, context.source)
        }

        return 1
    }
}