package net.rx.modules.git

import net.rx.modules.ArgumentTokenizer
import net.rx.modules.commands.*
import net.rx.modules.config.ConfigManager
import java.io.IOException
import java.util.concurrent.TimeUnit

object RawGitHandler {
    var executing: Boolean = false
        private set
    var executor: String? = null
        private set
    var command: String? = null
        private set

    private fun setAll(executing: Boolean, executor: String, command: String) {
        RawGitHandler.executing = executing
        RawGitHandler.executor = executor
        RawGitHandler.command = command
    }

    fun runGit(path: String, args: String, source: Source) {
        val pathToGitConfig = ConfigManager.dirPath
            .resolve("gitconfig")
            .resolve(source.player?.uuidAsString ?: "")


        if (ConfigManager.config.forceGitConfig && !pathToGitConfig.toFile().exists()) {
            /* TODO: Make this cleaner */
            source.sendFeedback(
                { red("Error. You do not have a gitconfig setup") }, false)

            source.sendFeedback(
                { gray("To create a gitconfig, you can use the /gitconfig command. Examples:") }, false)

            source.sendFeedback(
                { gray("  /gitconfig user.name ${source.player?.name}") }, false)

            source.sendFeedback(
                { gray("  /gitconfig user.email ${source.player?.name}@email.com") }, false)

            source.sendFeedback(
                { gray("  /gitconfig remote.origin.url ${source.player?.name}:<API TOKEN>@<GIT-REPO>") }, false)
        } else {
            source.sendFeedback(
                { gray("executing: git $args") }, true)

            runGit("git -c include.path=\"${pathToGitConfig.toAbsolutePath().toString()}\"  -C \"$path\" $args", source)
        }
    }

    fun runGit(cmd: String, source: Source) {
        setAll(true, source.player!!.name.string, cmd)

        val argv = ArgumentTokenizer.tokenize(cmd).toTypedArray()

        try {
            val proc = ProcessBuilder(*argv)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.SECONDS)
            val stdout = proc.inputStream.bufferedReader().readText().trim()
            val stderr = proc.errorStream.bufferedReader().readText().trim()

            if (stdout.isNotBlank()) {
                source.sendFeedback(
                    { green(stdout) }, false)
            }

            if (stderr.isNotBlank()) {
                source.sendFeedback(
                    { red(stderr) }, false)
            }

            if (stdout.isBlank() && stderr.isBlank()) {
                source.sendFeedback(
                    { green("GitCmd successfully executed (no output)") }, false)
            }

        } catch(e: IOException) {
            e.printStackTrace()
            source.sendFeedback(
                { red("git exception in code: $e") }, true)
        }

        executor = null
        command = null
        executing = false
    }
}