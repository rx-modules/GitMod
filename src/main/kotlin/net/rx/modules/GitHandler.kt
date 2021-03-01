package net.rx.modules

import net.rx.modules.commands.*
import net.rx.modules.config.ConfigManager
import java.io.IOException
import java.util.concurrent.TimeUnit

object GitHandler {
    var executing: Boolean = false
        private set
    var executor: String? = null
        private set
    var command: String? = null
        private set

    private fun setAll(executing: Boolean, executor: String, command: String) {
        this.executing = executing
        this.executor = executor
        this.command = command
    }

    fun runGit(path: String, args: String, source: Source) {
        source.sendFeedback(
            gray("executing: git $args"), true)

        val pathToGitConfig = ConfigManager.dirPath
            .resolve("gitconfig")
            .resolve("${source.player.uuidAsString}")
            .toAbsolutePath()

        runGit("git -c include.path=${pathToGitConfig.toString()}  -C \"$path\" $args", source)
    }

    fun runGit(cmd: String, source: Source) {
        setAll(true, source.player.entityName, cmd)

        val argv = ArgumentTokenizer.tokenize(cmd).toTypedArray()

        try {
            val proc = ProcessBuilder(*argv)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.SECONDS)
            val stdout = proc.inputStream.bufferedReader().readText().trim()
            val stderr = proc.errorStream.bufferedReader().readText().trim()

            if (!stdout.isNullOrBlank()) {
                source.sendFeedback(
                    green(stdout), false)
            }

            if (!stderr.isNullOrBlank()) {
                source.sendFeedback(
                    red(stderr), false)
            }

            if (stdout.isNullOrBlank() && stderr.isNullOrBlank()) {
                source.sendFeedback(
                    green("GitCmd successfully executed (no output)"), false)
            }

        } catch(e: IOException) {
            e.printStackTrace()
            source.sendFeedback(
                red("git exception in code: $e"), true)
        }

        executor = null
        command = null
        executing = false
    }
}