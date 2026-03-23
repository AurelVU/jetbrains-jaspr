package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationFactory
import com.intellij.execution.configurations.RuntimeConfigurationError
import org.jdom.Element

class JasprRunConfigurationTest : JasprTestBase() {

    private lateinit var config: JasprRunConfiguration

    override fun setUp() {
        super.setUp()
        val type = JasprConfigurationType()
        val factory = JasprRunConfigurationFactory(type)
        config = JasprRunConfiguration(project, factory, "Test")
    }

    fun testDefaultValues() {
        assertEquals("serve", config.command)
        assertEquals("8080", config.port)
        assertFalse(config.noSsr)
        assertEquals("", config.target)
        assertEquals("", config.additionalArgs)
    }

    fun testWriteAndReadExternal() {
        config.command = "build"
        config.port = "3000"
        config.noSsr = true
        config.target = "lib/app.dart"
        config.additionalArgs = "--verbose --release"

        val element = Element("configuration")
        config.writeExternal(element)

        val restored = createFreshConfig()
        restored.readExternal(element)

        assertEquals("build", restored.command)
        assertEquals("3000", restored.port)
        assertTrue(restored.noSsr)
        assertEquals("lib/app.dart", restored.target)
        assertEquals("--verbose --release", restored.additionalArgs)
    }

    fun testReadExternalWithEmptyElement() {
        val element = Element("configuration")
        config.readExternal(element)

        assertEquals("serve", config.command)
        assertEquals("8080", config.port)
        assertFalse(config.noSsr)
        assertEquals("", config.target)
        assertEquals("", config.additionalArgs)
    }

    fun testSerializationRoundtrip() {
        config.command = "serve"
        config.port = "9090"
        config.noSsr = false
        config.target = "web/main.dart"
        config.additionalArgs = "--debug"

        val element = Element("configuration")
        config.writeExternal(element)

        val restored = createFreshConfig()
        restored.readExternal(element)

        val element2 = Element("configuration")
        restored.writeExternal(element2)

        assertEquals(
            element.getAttributeValue("jaspr-command"),
            element2.getAttributeValue("jaspr-command")
        )
        assertEquals(
            element.getAttributeValue("jaspr-port"),
            element2.getAttributeValue("jaspr-port")
        )
        assertEquals(
            element.getAttributeValue("jaspr-no-ssr"),
            element2.getAttributeValue("jaspr-no-ssr")
        )
        assertEquals(
            element.getAttributeValue("jaspr-target"),
            element2.getAttributeValue("jaspr-target")
        )
        assertEquals(
            element.getAttributeValue("jaspr-additional-args"),
            element2.getAttributeValue("jaspr-additional-args")
        )
    }

    fun testCheckConfigurationThrowsOnEmptyCommand() {
        config.command = ""
        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("must not be empty"))
        }
    }

    fun testCheckConfigurationThrowsOnBlankCommand() {
        config.command = "   "
        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("must not be empty"))
        }
    }

    fun testCheckConfigurationSucceedsOnValidCommand() {
        config.command = "serve"
        config.checkConfiguration() // Should not throw
    }

    private fun createFreshConfig(): JasprRunConfiguration {
        val type = JasprConfigurationType()
        val factory = JasprRunConfigurationFactory(type)
        return JasprRunConfiguration(project, factory, "Test")
    }
}
