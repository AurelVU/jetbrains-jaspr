package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JasprConfigurationTypeTest : BasePlatformTestCase() {

    fun testConfigurationTypeId() {
        val type = JasprConfigurationType()
        assertEquals("JasprRunConfiguration", type.id)
    }

    fun testHasFactory() {
        val type = JasprConfigurationType()
        val factories = type.configurationFactories
        assertTrue("Should have at least one factory", factories.isNotEmpty())
    }

    fun testFactoryCreatesJasprRunConfiguration() {
        val type = JasprConfigurationType()
        val factory = type.configurationFactories.first()
        val config = factory.createTemplateConfiguration(project)
        assertTrue(
            "Factory should create JasprRunConfiguration",
            config is JasprRunConfiguration
        )
    }
}
