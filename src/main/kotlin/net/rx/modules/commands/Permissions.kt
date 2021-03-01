package net.rx.modules.commands

import net.rx.modules.config.ConfigManager

object Permissions {
    fun checkOperatorPermission(source: Source): Boolean {
        return ConfigManager.getOperators().contains(source.player.uuidAsString)
    }
}