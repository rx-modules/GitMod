package net.rx.modules.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.MessageArgumentType
import net.minecraft.server.PlayerManager
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.util.Formatting
import net.rx.modules.config.ConfigManager

object GitAdminCommand : Command() {
    override fun register(dispatcher: Dispatcher) {
        val gitAdminNode = CommandManager
            .literal("gitadmin")
            .requires { it.hasPermissionLevel(4) }
            .build()

        val gitReloadNode = CommandManager
            .literal("reload")
            .executes{ reload(it)}
            .build()

        val gitOperatorNode = CommandManager
            .literal("operator")
            .build()

        val operatorAddArg = CommandManager
            .argument("player", EntityArgumentType.player())
            .executes { addOperator(it, EntityArgumentType.getPlayer(it, "player")) }

        val operatorRemoveArg = CommandManager
            .argument("player", EntityArgumentType.player())
            .executes { removeOperator(it, EntityArgumentType.getPlayer(it, "player")) }

        val gitOperatorAddNode = CommandManager
            .literal("add")
            .executes{ addOperator(it, it.source.player!!) }
            .then(operatorAddArg)
            .build()

        val gitOperatorRemoveNode = CommandManager
            .literal("remove")
            .then(operatorRemoveArg)
            .build()

        val gitOperatorListNode = CommandManager
            .literal("list")
            .executes(::listOperators)
            .build()

        dispatcher.root.addChild(gitAdminNode)

        gitAdminNode.addChild(gitReloadNode)
        gitAdminNode.addChild(gitOperatorNode)

        gitOperatorNode.addChild(gitOperatorAddNode)
        gitOperatorNode.addChild(gitOperatorRemoveNode)
        gitOperatorNode.addChild(gitOperatorListNode)
    }

    private fun reload(context: Context): Int {
        ConfigManager.reloadData()
        context.source.sendFeedback(
            { gray("Successfully reloaded configuration") }, true)

        return 1
    }

    private fun addOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.addOperator(player.name.string, player.uuid.toString())

        if (out == 1) {
            context.source.sendFeedback(
                { gray("Successfully added ${player.name} as an operator") }, true
            )
            context.source.player?.server?.playerManager?.sendCommandTree(context.source.player)
        }
        else
            context.source.sendFeedback(
                { red("Could not added, ${player.name} is already an operator") }, true)
        return 1
    }

    private fun removeOperator(context: Context, player: ServerPlayerEntity): Int {
        val out = ConfigManager.removeOperator(player.name.string, player.uuid.toString())

        if (out == 1) {
            context.source.sendFeedback(
                { gray("Successfully removed ${player.name.string} as an operator") }, true
            )
            context.source.player?.server?.playerManager?.sendCommandTree(context.source.player)
        }
        else
            context.source.sendFeedback(
                { red("Could not remove, ${player.name} is not an operator") }, true)

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
            { gray("Operators: ${ConfigManager.getOperators().joinToString(",")}") }, false)
        return 1
    }
}