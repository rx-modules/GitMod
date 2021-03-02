package net.rx.modules.commands

abstract class Command {
    open fun register(dispatcher: Dispatcher) {}

    companion object {
        fun registerAll(dispatcher: Dispatcher) {
            RawGitCommand.register(dispatcher)
            GitAdminCommand.register(dispatcher)
            GitConfigCommand.register(dispatcher)
        }
    }
}