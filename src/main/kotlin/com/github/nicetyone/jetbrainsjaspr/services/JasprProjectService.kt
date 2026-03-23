package com.github.nicetyone.jetbrainsjaspr.services

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
class JasprProjectService(private val project: Project) {
    private val cachedResult = AtomicReference<Boolean?>(null)

    init {
        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    if (events.any { it.file?.name == "pubspec.yaml" }) {
                        cachedResult.set(null)
                    }
                }
            }
        )
    }

    fun isJasprProject(): Boolean {
        cachedResult.get()?.let { return it }

        val result = detectJaspr()
        cachedResult.set(result)
        return result
    }

    private fun detectJaspr(): Boolean {
        val baseDir = project.basePath ?: return false
        return ReadAction.compute<Boolean, Exception> {
            val pubspec = VirtualFileManager.getInstance()
                .findFileByUrl("file://$baseDir/pubspec.yaml") ?: return@compute false
            try {
                val content = String(pubspec.contentsToByteArray(), Charsets.UTF_8)
                content.contains("jaspr:") || content.contains("jaspr_builder:")
            } catch (_: Exception) {
                false
            }
        }
    }

    internal fun resetCache() {
        cachedResult.set(null)
    }

    fun findPubspecFile(): VirtualFile? {
        val baseDir = project.basePath ?: return null
        return ReadAction.compute<VirtualFile?, Exception> {
            VirtualFileManager.getInstance()
                .findFileByUrl("file://$baseDir/pubspec.yaml")
        }
    }

    companion object {
        fun getInstance(project: Project): JasprProjectService =
            project.getService(JasprProjectService::class.java)

        fun isJasprProject(project: Project): Boolean =
            getInstance(project).isJasprProject()
    }
}
