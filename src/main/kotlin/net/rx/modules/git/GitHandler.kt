package net.rx.modules.git

import net.minecraft.text.LiteralText
import net.rx.modules.config.ConfigManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

const val LOG_COUNT: Int = 5

object GitHandler {
    lateinit var repo: Repository

    private val head: ObjectId
        get() { return repo.resolve(Constants.HEAD) }

    private fun openGit(): Git? {
        return Git.open(File(ConfigManager.getGitPath()))
    }

    fun add(dir: String) {
        with (openGit()) {
            this?.add()?.addFilepattern(dir)?.call()
        }
    }

    fun log() {
        val logs: Iterable<RevCommit>?
        with (openGit()) {
            // If we do have a git repo, then we can expect the log command to have thingss
            logs = this?.log()!!.add(head).setMaxCount(LOG_COUNT).call()
        }

        val out = LiteralText("")
        logs?.forEach { out.append(LiteralText(it.shortMessage).styled { it } ) }

    }
}