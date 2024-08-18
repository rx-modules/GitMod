package net.rx.modules.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.rx.modules.config.ConfigManager
import java.util.concurrent.CompletableFuture

/*
Handy aliases to use through out command code
 */

typealias Context = CommandContext<ServerCommandSource>
typealias Dispatcher = CommandDispatcher<ServerCommandSource>
typealias Source = ServerCommandSource


fun invalidCommand(context: Context, msg: String): Int {
    context.source.sendFeedback({ red(msg) }, false)
    return 0
}

fun infoMessage(context: Context, msg: Text): Int {
    context.source.sendFeedback(
        { msg }, false)
    return 0
}

//fun suggestFactory(suggestions: List<String>): SuggestionProvider<Source> {
//    return SuggestionProvider<Source> { context, builder ->
//        // Suggestions for common git sub-commands to run
//        suggestions.map { builder.suggest(it) }
//
//        builder.buildFuture()
//    }
//}


fun red(string: String): MutableText = Text.literal(string).styled { Style.EMPTY.withColor(Formatting.RED) }
fun green(string: String): MutableText = Text.literal(string).styled { Style.EMPTY.withColor(Formatting.GREEN) }
fun gray(string: String): MutableText = Text.literal(string).styled { Style.EMPTY.withColor(Formatting.GRAY) }
