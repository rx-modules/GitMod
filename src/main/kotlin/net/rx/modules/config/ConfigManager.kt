package net.rx.modules.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.rx.modules.GitHandler
import net.rx.modules.commands.Context
import net.rx.modules.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


/**
 * Manages data concerning [config][config]
 */
object ConfigManager {
    private const val name = "config.json"

    private var registered: Boolean = false

    lateinit var dirPath: Path
        private set

    private lateinit var configPath: Path

    private lateinit var config: Config

    private lateinit var server: MinecraftServer

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
        dirPath = server
            .runDirectory.toPath()
            .resolve("config")
            .resolve("gitmod")

        Files.createDirectories(dirPath.resolve("gitconfig"))

        configPath = dirPath.resolve(name)
        with(configPath.toFile()) {
            if (!this.exists()) initData(this)
            else loadAllData()
        }
    }

    private fun initData(dataFile: File) {
        logger.info("Creating Config File")
        dataFile.writeText("")

        config = Config()
        fixGitPath()
        writeToFile(dataFile, config)
    }

    fun reloadData() {
        with(configPath.toFile()) {
            if (!this.exists()) initData(this)
            else loadAllData()
        }
    }


    private fun loadAllData() {
        logger.info("Loading all data..")
        // println(dataPath.toAbsolutePath().toString())
        config = readFromFile(configPath.toFile())

        logger.debug("Operators: ")
        config.operators.forEach { op -> logger.debug(op.name) }
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
            logger.warn("Invalid JSON. Replacing with default")
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
     * Gets all the uuids of the operators
     *
     * @return list of keys in the cache converted to uuids
     */
    fun getOperators(): Set<String> {
        return config.operators.map{ it.name }.toSet()
    }

    /**
     * Gets the defined gitpath in the config
     *
     * @return Path to git repo
     */
    fun getGitPath(): String {
        return config.gitPath
    }

    fun addOperator(name: String, uuid: String): Int {
        if (getOperators().contains(uuid)) {
            return 0
        }

        config.operators.add(Operator(name, uuid))
        return 1
    }

    fun removeOperator(name: String, uuid: String): Int {
        if (!getOperators().contains(uuid)) {
            return 0
        }

        config.operators = config.operators.filter { it.name != name } as MutableList<Operator>
        return 1
    }
}