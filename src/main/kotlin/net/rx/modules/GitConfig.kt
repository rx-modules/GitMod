package net.rx.modules

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.*
import kotlinx.serialization.json.Json
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Manages data concerning [config][config]
 */
object GitConfig : ConfigManager() {
    override val dataSpec = Config::class

    override val fileExtension: String = "json"

    override val enableSaveOnShutDown: Boolean = true

    override val enableLoadAllOnStart: Boolean = false


    override fun readFromFile(dataString: String): Config {
        return Json.decodeFromString(Json { prettyPrint = true}.encodeToString(Config.serializer() as Config))
    }

    override fun writeToFile(dataFile: File, data: Config) {
        dataFile.writeText(Json{ prettyPrint = true }.encodeToString(Config.serializer(), data))
    }

    /**
     * Gets all the uuids of the builders that are currently online
     *
     * @return list of keys in the cache converted to uuids
     */
    fun getOnlineOperators(): Set<String> {
        println("getting online ops yooo")
        println(cache["gitmod"]?.operators?.keys)
        return cache["gitmod"]?.operators?.keys ?: emptySet<String>()
    }

    /**
     * Gets the defined gitpath in the config
     *
     * @return Path to git repo
     */
    fun getGitPath(): String? {
        return cache["gitmod"]?.gitPath
    }
}