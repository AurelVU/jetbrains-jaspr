package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings

class JasprSettingsTest : JasprTestBase() {

    fun testDefaultValue() {
        val settings = JasprSettings.getInstance(project)
        assertTrue("Default CLI path should be empty", settings.jasprCliPath.isEmpty())
    }

    fun testSetAndGet() {
        val settings = JasprSettings.getInstance(project)
        settings.jasprCliPath = "/usr/local/bin/jaspr"
        assertEquals("/usr/local/bin/jaspr", settings.jasprCliPath)
    }

    fun testStateRoundtrip() {
        val settings = JasprSettings.getInstance(project)
        settings.jasprCliPath = "/custom/path/jaspr"

        val state = settings.state
        assertEquals("/custom/path/jaspr", state.jasprCliPath)

        val newSettings = JasprSettings()
        newSettings.loadState(state)
        assertEquals("/custom/path/jaspr", newSettings.jasprCliPath)
    }

    fun testNullState() {
        val settings = JasprSettings()
        val state = settings.state
        assertNotNull(state)
        assertTrue("CLI path should be empty on fresh instance", settings.jasprCliPath.isEmpty())
    }
}
