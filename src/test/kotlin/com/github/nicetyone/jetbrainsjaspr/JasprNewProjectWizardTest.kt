package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.wizard.JasprNewProjectWizard
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JasprNewProjectWizardTest : BasePlatformTestCase() {

    private lateinit var wizard: JasprNewProjectWizard

    override fun setUp() {
        super.setUp()
        wizard = JasprNewProjectWizard()
    }

    fun testWizardId() {
        assertEquals("jaspr", wizard.id)
    }

    fun testWizardNameAndIcon() {
        assertEquals("Jaspr", wizard.name)
        assertNotNull(wizard.icon)
    }

    fun testWizardDescriptionIsNotEmpty() {
        assertTrue(wizard.description.isNotEmpty())
    }
}
