package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

abstract class JasprTestBase : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    override fun setUp() {
        super.setUp()
        // Clean up state from previous tests (shared light project)
        cleanupPubspec()
        JasprProjectService.getInstance(project).resetCache()
        JasprSettings.getInstance(project).jasprCliPath = ""
    }

    protected fun setupJasprProject() {
        writePubspec(
            """
            name: test_app
            dependencies:
              jaspr: ^0.1.0
            """.trimIndent()
        )
    }

    protected fun setupJasprBuilderProject() {
        writePubspec(
            """
            name: test_app
            dev_dependencies:
              jaspr_builder: ^0.1.0
            """.trimIndent()
        )
    }

    protected fun setupNonJasprProject() {
        writePubspec(
            """
            name: test_app
            dependencies:
              flutter: sdk: flutter
            """.trimIndent()
        )
    }

    private fun writePubspec(content: String) {
        val basePath = project.basePath ?: error("Project basePath is null")
        // First clean up any existing file
        cleanupPubspec()
        // Write new file
        val pubspecFile = File(basePath, "pubspec.yaml")
        pubspecFile.parentFile?.mkdirs()
        pubspecFile.writeText(content)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(pubspecFile)
        // Reset cache so the service re-detects
        JasprProjectService.getInstance(project).resetCache()
    }

    private fun cleanupPubspec() {
        val basePath = project.basePath ?: return
        // Delete via VFS to ensure VFS cache is updated
        val vFile = LocalFileSystem.getInstance().findFileByPath("$basePath/pubspec.yaml")
        if (vFile != null) {
            runWriteAction { vFile.delete(this) }
        }
        // Also delete from disk in case VFS didn't track it
        val ioFile = File(basePath, "pubspec.yaml")
        if (ioFile.exists()) {
            ioFile.delete()
            LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath)
        }
    }

    override fun tearDown() {
        try {
            cleanupPubspec()
            JasprProjectService.getInstance(project).resetCache()
        } finally {
            super.tearDown()
        }
    }
}
