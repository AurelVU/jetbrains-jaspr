package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationEditor
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationFactory

class JasprRunConfigurationEditorTest : JasprTestBase() {

    private lateinit var editor: JasprRunConfigurationEditor

    private fun createConfig(): JasprRunConfiguration {
        val type = JasprConfigurationType()
        val factory = JasprRunConfigurationFactory(type)
        return JasprRunConfiguration(project, factory, "Test")
    }

    override fun setUp() {
        super.setUp()
        editor = JasprRunConfigurationEditor(project)
        editor.component // trigger createEditor
    }

    fun testResetEditorFromFillsFields() {
        val config = createConfig()
        config.command = "build"
        config.port = "3000"
        config.noSsr = true
        config.target = "lib/app.dart"
        config.additionalArgs = "--verbose"

        editor.resetFrom(config)

        val restored = createConfig()
        editor.applyTo(restored)

        assertEquals("build", restored.command)
        assertEquals("3000", restored.port)
        assertTrue(restored.noSsr)
        assertEquals("lib/app.dart", restored.target)
        assertEquals("--verbose", restored.additionalArgs)
    }

    fun testApplyEditorToReadsFields() {
        val config = createConfig()
        config.command = "clean"
        config.port = "9090"
        config.noSsr = false
        config.target = ""
        config.additionalArgs = ""

        editor.resetFrom(config)

        val output = createConfig()
        editor.applyTo(output)

        assertEquals("clean", output.command)
        assertEquals("9090", output.port)
        assertFalse(output.noSsr)
        assertEquals("", output.target)
        assertEquals("", output.additionalArgs)
    }

    fun testRoundtripThroughEditor() {
        val original = createConfig()
        original.command = "serve"
        original.port = "4000"
        original.noSsr = true
        original.target = "web/main.dart"
        original.additionalArgs = "--release --debug"

        editor.resetFrom(original)

        val restored = createConfig()
        editor.applyTo(restored)

        assertEquals(original.command, restored.command)
        assertEquals(original.port, restored.port)
        assertEquals(original.noSsr, restored.noSsr)
        assertEquals(original.target, restored.target)
        assertEquals(original.additionalArgs, restored.additionalArgs)
    }
}
