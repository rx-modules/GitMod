package net.rx.modules

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import java.nio.file.Files
import java.nio.file.Path

/**
 * Manages data concerning [config][config]
 */
object GitConfig {
    const val name = "config.json"

    private var registered: Boolean = false

    private lateinit var dataDir: Path

    lateinit var config: Config

    fun register(server: MinecraftServer) {
        if (!registered) initialize(server)

        ServerLifecycleEvents.SERVER_STARTING.register {
            initialize(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {

        }

        registered = true
    }

    private fun initialize(server: MinecraftServer) {
        // Sets data directory to the correct
        // folder within the Fabric Zones
        // directory
        dataDir = server
            .runDirectory.toPath()
            .resolve("config")
            .resolve("gitmod")

        Files.createDirectories(dataDir)

        loadAllData(server)
    }

    private fun loadAllData(server: MinecraftServer) {
        config = readFromFile(dataDir.toString())

        // fix data
        if (config.gitPath.isNullOrBlank()) {
            config.gitPath = server
                .getSavePath(WorldSavePath.DATAPACKS).toString()
        }
    }


    private fun readFromFile(dataString: String): Config {
        return Json.decodeFromString(Config.serializer(), dataString)
    }

    private fun writeToFile(dataFile: File, data: Config) {
        dataFile.writeText(Json{ prettyPrint = true }.encodeToString(Config.serializer(), data))
    }

    /**
     * Gets all the uuids of the builders that are currently online
     *
     * @return list of keys in the cache converted to uuids
     */
    fun getOnlineOperators(): Set<String> {
        println("getting online ops yooo")
        println(config.operators)
        return config.operators.keys
    }

    /**
     * Gets the defined gitpath in the config
     *
     * @return Path to git repo
     */
    fun getGitPath(): String {
        return config.gitPath
    }
}