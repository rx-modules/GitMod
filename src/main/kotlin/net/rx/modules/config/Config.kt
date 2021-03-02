package net.rx.modules.config

import kotlinx.serialization.Serializable


@Serializable
data class Config(
    /**
     * Describes the data inside the configuration file
     * Used for easy serialialization
     */
    val id: String = "gitmod",

    // path to repo
    var gitPath: String = "",

    // operators to handle, UUID: name
    var operators: MutableList<Operator> = mutableListOf(),

    // force personal git configs
    var forceGitConfig: Boolean = false

    // allows the use of other directories
    // var otherGitPaths: MutableMap<String, String> = mutableMapOf()
)

@Serializable
data class Operator(
    /**
     * Describes an operator
     */
    val name: String = "",
    val uuid: String = "",
    val accessToRawGit: Boolean = false,
)
