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
fun dark_gray(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))

class HomeCommand(private val dispatcher: CommandDispatcher<ServerCommandSource?>) {

    var executing: Boolean = false
    var executor: String? = null
    var command: String? = null

    fun register() {
        dispatcher.register(
            literal("git")
                .requires ( ::checkOperatorPermission )
                .executes { invalidCommand(it, "Invalid invocation. Try /git status") }
                .then(
                    argument("args", StringArgumentType.greedyString())
                        .executes { gitCommand(it, StringArgumentType.getString(it, "args")) }
                )
        )

        dispatcher.register(
            literal("gitconfig")
                .requires { it.hasPermissionLevel(4) }
                .executes { invalidCommand(it, "Invalid invocation. Try /gitconfig reload") }
                .then(
                    literal("reload").executes(::reloadCommand)
                )
        )
    }

    private fun checkOperatorPermission(context: ServerCommandSource): Boolean {
        return GitConfig.getOnlineOperators().contains(context.player.uuidAsString)
    }

    private fun reloadCommand(context: CommandContext<ServerCommandSource>): Int {
        GitConfig.reloadData()
        context.source.sendFeedback(
            dark_gray("Successfully reloaded GitConfig"), true)
        return 1
    }

    private fun invalidCommand(context: CommandContext<ServerCommandSource>, msg: String): Int {
        context.source.sendFeedback(
            red(msg), false)
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
            dark_gray("executing: $cmd"), true)

        GlobalScope.launch(Dispatchers.IO) {
            executor = context.source.player.entityName
            command = cmd
            executing = true

            runGit(cmd, context.source)
        }

        return 0
    }

    private fun runGit(cmd: String, source: ServerCommandSource) {
        val argv = cmd.split(" ").toTypedArray()

        try {
            val proc = ProcessBuilder(*argv)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.SECONDS)
            val stdout = proc.inputStream.bufferedReader().readText().trim()
            val stderr = proc.errorStream.bufferedReader().readText().trim()

            if (stdout.isNullOrBlank()) {
                source.sendFeedback(
                    green(stdout), false)
            }

            if (!stderr.isNullOrBlank()) {
                source.sendFeedback(
                    red(stderr), false)
            }

            if (stdout.isNullOrBlank() && stderr.isNullOrBlank()) {
                source.sendFeedback(
                    green("GitCmd successfully executed (no output)"), false)
            }

        } catch(e: IOException) {
            e.printStackTrace()
            source.sendFeedback(
                red("git exception in code: $e"), true)
        }

        executor = null
        command = null
        executing = false
    }

}
