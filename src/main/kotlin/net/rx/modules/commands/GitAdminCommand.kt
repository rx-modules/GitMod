package net.rx.modules.commands

import com.github.p03w.aegis.AegisCommandBuilder
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.network.ServerPlayerEntity
import net.rx.modules.config.ConfigManager

object GitAdminCommand : Command() {
    override fun register(dispatcher: Dispatcher) {
        dispatcher.register(
            AegisCommandBuilder("gitadmin") {
                requires { it.hasPermissionLevel(4) }

                literal("reload") {
                    executes(::reload)
                }

                literal("operator") {
                    literal("add") {
                        executes { addOperator(it, it.source.player) }
                        string("player") {
                            executes {
                                addOperator(it, EntityArgumentType.getPlayer(it, "player"))
                            }
                        }
                    }

                    literal("remove") {
                        executes { removeOperator(it, it.source.player) }
                        string("player") {
                            executes {
                                removeOperator(it, EntityArgumentType.getPlayer(it, "player"))
                            }
                        }
                    }

                }
            }.build()
        )
    }

    private fun reload(context: Context): Int {
        ConfigManager.reloadData()
        context.source.sendFeedback(
            gray("Successfully reloaded configuration"), true)
        return 1
    }

    private fun addOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.removeOperator(player.entityName, player.uuid.toString())

        if (out == 1)
            context.source.sendFeedback(
                gray("Successfully added ${player.entityName} as an operator"), true)
        else
            context.source.sendFeedback(
                red("Could not added, ${player.entityName} is already an operator"), true)
        return 1
    }

    private fun removeOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.addOperator(player.entityName, player.uuid.toString())

        if (out == 1)
            context.source.sendFeedback(
                gray("Successfully removed ${player.entityName} as an operator"), true)
        else
            context.source.sendFeedback(
                red("Could not remove, ${player.entityName} is not an operator"), true)

        return out
    }
}