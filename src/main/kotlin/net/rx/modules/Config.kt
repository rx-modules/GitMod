package net.rx.modules

import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import java.nio.file.Files

/**
 * A data specification intended to be extended by
 * a data class. Only requires an [id] property
 */
@Serializable
data class Config(
    /**
     * Used as the file name of an instance of the data spec
     * if saved with a [data manager][ConfigManager]
     */
    val id: String = "gitmod",

    // path to repo
    val gitPath: String = "",

    // operators to handle, UUID: name
    val operators: MutableMap<String, String> = mutableMapOf()

)
