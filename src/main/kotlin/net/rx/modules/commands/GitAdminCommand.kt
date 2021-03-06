package net.rx.modules.commands

import com.github.p03w.aegis.aegisCommand
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.util.Formatting
import net.rx.modules.config.ConfigManager
import net.rx.modules.text

object GitAdminCommand : Command() {
    override fun register(dispatcher: Dispatcher) {
        dispatcher.register(
            aegisCommand("gitadmin") {
                requires { it.hasPermissionLevel(4) }

                literal("reload") {
                    executes { reload(it) }
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

//                    literal("set") {
//                        string("player") {
//                            string("player") {
//                                executes {
//                                    setOperator(it, EntityArgumentType.getPlayer(it, "player"))
//                                }
//                            }
//                        }
//                    }

                    literal("list") {
                        executes { listOperators(it) }
                    }

                }
            }
        )
    }

    private fun reload(context: Context): Int {
        ConfigManager.reloadData()
        val test = text {
            "hello world" styled Formatting.BOLD + Formatting.GREEN
            NEW_LINE
            "a more complex string" {
                NEW_LINE
                "testing" onClick {
                    action = ClickEvent.Action.RUN_COMMAND
                    value = "/git status"
                }
            } styled Formatting.RED
            -"testing"
        }
        context.source.sendFeedback(
            gray("Successfully reloaded configuration"), true)
        context.source.sendFeedback(test, true)
        return 1
    }

    private fun addOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.addOperator(player.entityName, player.uuid.toString())

        if (out == 1) {
            context.source.sendFeedback(
                gray("Successfully added ${player.entityName} as an operator"), true
            )
            context.source.player.server.playerManager.sendCommandTree(context.source.player)
        }
        else
            context.source.sendFeedback(
                red("Could not added, ${player.entityName} is already an operator"), true)
        return 1
    }

    private fun removeOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.removeOperator(player.entityName, player.uuid.toString())

        if (out == 1) {
            context.source.sendFeedback(
                gray("Successfully removed ${player.entityName} as an operator"), true
            )
            context.source.player.server.playerManager.sendCommandTree(context.source.player)
        }
        else
            context.source.sendFeedback(
                red("Could not remove, ${player.entityName} is not an operator"), true)

        return out
    }

//    private fun setOperator(context: Context, player: ServerPlayerEntity): Int {
//        val out = ConfigManager.setOperator(player.entityName, player.uuid.toString())
//
//        if (out == 1) {
//            context.source.sendFeedback(
//                gray("Successfully removed ${player.entityName} as an operator"), true
//            )
//            context.source.player.server.playerManager.sendCommandTree(context.source.player)
//        }
//        else
//            context.source.sendFeedback(
//                red("Could not remove, ${player.entityName} is not an operator"), true)
//
//        return out
//    }

    private fun listOperators(context: Context): Int {
        context.source.sendFeedback(
            gray("Operators: ${ConfigManager.getOperators().joinToString(",")}"), false)
        return 1
    }
}