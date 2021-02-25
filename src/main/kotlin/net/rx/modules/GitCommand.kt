package net.rx.modules

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun red(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.RED))
fun green(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.GREEN))

class HomeCommand(private val dispatcher: CommandDispatcher<ServerCommandSource?>) {
    fun register() {
        dispatcher.register(
            literal("git")
                //.requires { it.hasPermissionLevel(4)}
                .requires { GitConfig.getOnlineOperators()!!.contains(it.toString()) }
                .executes ( ::invalidCommand )
                .then(
                    argument("args", StringArgumentType.greedyString())
                        .executes { gitCommand(it, StringArgumentType.getString(it, "args")) }
                )
        )
    }

    private fun invalidCommand(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback(
            red("Invalid use of git command. Requires arguments."), false)
        return 0
    }

    private fun gitCommand(context: CommandContext<ServerCommandSource>, args: String): Int {
        val path = GitConfig.getGitPath()
        val argv = "git -C $path $args".split(" ").toTypedArray()

        try {
            val proc = ProcessBuilder(*argv)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.SECONDS)
            val stdout = proc.inputStream.bufferedReader().readText()
            val stderr = proc.errorStream.bufferedReader().readText()
            context.source.sendFeedback(
                green(stdout), true)
            context.source.sendFeedback(
                red(stderr), true)
        } catch(e: IOException) {
            e.printStackTrace()
            context.source.sendFeedback(
                red("git exception in code: $e"), true)
            return 0
        }

        return 1
    }

}
