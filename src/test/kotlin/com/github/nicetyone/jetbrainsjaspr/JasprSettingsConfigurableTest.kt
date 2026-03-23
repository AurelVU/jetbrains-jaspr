package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.github.nicetyone.jetbrainsjaspr.settings.JasprSettingsConfigurable

class JasprSettingsConfigurableTest : JasprTestBase() {

    private lateinit var configurable: JasprSettingsConfigurable

    override fun setUp() {
        super.setUp()
        configurable = JasprSettingsConfigurable(project)
    }

    override fun tearDown() {
        try {
            configurable.disposeUIResources()
        } finally {
            super.tearDown()
        }
    }

    fun testCreateComponentReturnsNotNull() {
        val component = configurable.createComponent()
        assertNotNull(component)
    }

    fun testIsModifiedAfterChange() {
        JasprSettings.getInstance(project).jasprCliPath = ""
        configurable.createComponent()
        configurable.reset()

        assertFalse(configurable.isModified)

        // Simulate a change — we need to set the internal field via reset then modify
        JasprSettings.getInstance(project).jasprCliPath = "/new/path"
        // After settings change, the UI still has old value -> isModified
        assertTrue(configurable.isModified)
    }

    fun testApplySavesValue() {
        configurable.createComponent()
        JasprSettings.getInstance(project).jasprCliPath = ""
        configurable.reset()

        // Change settings externally to test apply restores from UI
        JasprSettings.getInstance(project).jasprCliPath = "/changed/path"
        // apply should write UI value (empty from reset) back to settings
        configurable.apply()
        assertEquals("", JasprSettings.getInstance(project).jasprCliPath)
    }
}
