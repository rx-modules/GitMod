package net.rx.modules.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.rx.modules.config.ConfigManager

/*
Handy aliases to use through out command code
 */

typealias Context = CommandContext<ServerCommandSource>
typealias Dispatcher = CommandDispatcher<ServerCommandSource>
typealias Source = ServerCommandSource


fun invalidCommand(context: Context, msg: String): Int {
    context.source.sendFeedback(
        red(msg), false)
    return 0
}

fun infoMessage(context: Context, msg: LiteralText): Int {
    context.source.sendFeedback(
        msg, false)
    return 0
}


fun red(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.RED))
fun green(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.GREEN))
fun gray(string: String): Text = LiteralText(string).setStyle(Style.EMPTY.withColor(Formatting.GRAY))
