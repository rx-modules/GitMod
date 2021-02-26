package net.rx.modules

import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.rx.modules.GitConfig.config
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


/**
 * Manages data concerning [config][config]
 */
object GitConfig {
    const val name = "config.json"

    private var registered: Boolean = false

    private lateinit var dataPath: Path

    lateinit var config: Config

    lateinit var server: MinecraftServer

    fun register(server: MinecraftServer) {
        this.server = server

        if (!registered) initialize()

        ServerLifecycleEvents.SERVER_STARTING.register {
            this.server = it
            initialize()
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {

        }

        registered = true
    }

    private fun initialize() {
        val dir = server
            .runDirectory.toPath()
            .resolve("config")

        Files.createDirectories(dir)

        dataPath = dir.resolve("gitmod.json")
        with(dataPath.toFile()) {
            if (!this.exists()) initData(this)
            else loadAllData()
        }
    }

    private fun initData(dataFile: File) {
        println("[GitMod] Creating Config File")
        dataFile.writeText("")

        config = Config()
        fixGitPath()
        writeToFile(dataFile, config)
    }

    fun reloadData() {
        with(dataPath.toFile()) {
            if (!this.exists()) initData(this)
            else loadAllData()
        }
    }


    fun loadAllData() {
        println("[GitMod] Loading all data..")
        // println(dataPath.toAbsolutePath().toString())
        config = readFromFile(dataPath.toFile())

        println("[GitMod] Operators: ")
        config.operators.keys.forEach { key -> println("[GitMod] $key") }
        println()

        fixGitPath()
    }

    private fun fixGitPath() {
        if (config.gitPath.isNullOrBlank()) {
            config.gitPath = server
                .getSavePath(WorldSavePath.DATAPACKS).toString()
        }
    }


    private fun readFromFile(dataFile: File): Config {
        return try {
            Json.decodeFromString(Config.serializer(), dataFile.readText())
        } catch (e : Throwable) {
            println("[GitMod] Invalid JSON. Replacing with default")
            config = Config()
            fixGitPath()
            writeToFile(dataFile, config)

            config
        }
    }

    private fun writeToFile(dataFile: File, data: Config) {
        dataFile.writeText(Json { prettyPrint = true }.encodeToString(Config.serializer(), data))
    }

    /**
     * Gets all the uuids of the operators that are currently online
     *
     * @return list of keys in the cache converted to uuids
     */
    fun getOnlineOperators(): Set<String> {
        return config.operators.values.toSet()
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