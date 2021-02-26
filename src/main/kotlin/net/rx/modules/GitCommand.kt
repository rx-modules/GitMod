package net.rx.modules

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.io.IOException
import java.util.concurrent.TimeUnit

fun red(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.RED))
fun green(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.GREEN))

class HomeCommand(private val dispatcher: CommandDispatcher<ServerCommandSource?>) {

    var executing: Boolean = false
    var executor: String? = null
    var command: String? = null

    fun register() {
        dispatcher.register(
            literal("git")
                .requires ( ::checkOperatorPermission )
                .executes ( ::invalidCommand )
                .then(
                    argument("args", StringArgumentType.greedyString())
                        .executes { gitCommand(it, StringArgumentType.getString(it, "args")) }
                )
        )

        dispatcher.register(
            literal("gitreload")
                .requires ( ::checkOperatorPermission )
                .executes ( ::reloadCommand )
        )
    }

    private fun checkOperatorPermission(context: ServerCommandSource): Boolean {
        return GitConfig.getOnlineOperators().contains(context.player.uuidAsString)
    }

    private fun reloadCommand(context: CommandContext<ServerCommandSource>): Int {
        GitConfig.loadAllData()
        context.source.sendFeedback(
            LiteralText("Successfully reloaded GitConfig"), true)
        return 1
    }

    private fun invalidCommand(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback(
            red("Invalid use of git command. Requires arguments."), false)
        return 0
    }

    private fun gitCommand(context: CommandContext<ServerCommandSource>, args: String): Int {
        if (executing) {
            context.source.sendFeedback(
                red("$executor is current running $command. Please wait.."), true)
            return 0
        }

        val path = GitConfig.getGitPath()
        var cmd = "git -C $path $args"

        context.source.sendFeedback(
            LiteralText("executing: $cmd"), true)

        GlobalScope.launch(Dispatchers.IO) {
            executor = context.source.player.entityName
            command = cmd
            executing = true

            runGit(cmd, context.source.player)
        }

        return 1
    }

    private fun runGit(cmd: String, player: ServerPlayerEntity) {
        val argv = cmd.split(" ").toTypedArray()

        try {
            val proc = ProcessBuilder(*argv)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.SECONDS)
            val stdout = proc.inputStream.bufferedReader().readText()
            val stderr = proc.errorStream.bufferedReader().readText()
            player.sendMessage(
                green(stdout), false)
            player.sendMessage(
                red(stderr), false)
        } catch(e: IOException) {
            e.printStackTrace()
            player.sendMessage(
                red("git exception in code: $e"), false)
        }

        executor = null
        command = null
        executing = false
    }

}
