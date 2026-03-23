package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationFactory
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationProducer
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import java.io.File

class JasprRunConfigurationProducerTest : JasprTestBase() {

    private fun createConfig(): JasprRunConfiguration {
        val type = JasprConfigurationType()
        val factory = JasprRunConfigurationFactory(type)
        return JasprRunConfiguration(project, factory, "Test")
    }

    private fun createContextForPubspec(): ConfigurationContext? {
        val basePath = project.basePath ?: return null
        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(basePath, "pubspec.yaml"))
            ?: return null
        val psiFile = PsiManager.getInstance(project).findFile(vFile) ?: return null
        return ConfigurationContext(psiFile)
    }

    /**
     * Helper to call the protected setupConfigurationFromContext via reflection.
     */
    private fun callSetup(
        producer: JasprRunConfigurationProducer,
        config: JasprRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val method = producer.javaClass.getDeclaredMethod(
            "setupConfigurationFromContext",
            com.intellij.execution.configurations.RunConfiguration::class.java,
            ConfigurationContext::class.java,
            Ref::class.java
        )
        method.isAccessible = true
        val ref = Ref<PsiElement>()
        return method.invoke(producer, config, context, ref) as Boolean
    }

    fun testSetupFromContextOnJasprProject() {
        setupJasprProject()
        val producer = JasprRunConfigurationProducer()
        val config = createConfig()
        val context = createContextForPubspec() ?: run {
            fail("Could not create context for pubspec.yaml")
            return
        }

        assertTrue(callSetup(producer, config, context))
        assertEquals("serve", config.command)
        assertEquals("jaspr serve", config.name)
    }

    fun testSetupFromContextOnNonJasprProject() {
        setupNonJasprProject()
        val producer = JasprRunConfigurationProducer()
        val config = createConfig()
        val context = createContextForPubspec() ?: run {
            fail("Could not create context for pubspec.yaml")
            return
        }

        assertFalse(callSetup(producer, config, context))
    }

    fun testSetupFromContextOnNonPubspecFile() {
        setupJasprProject()

        val basePath = project.basePath ?: error("No basePath")
        val dartFile = File(basePath, "main.dart")
        dartFile.writeText("void main() {}")
        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dartFile) ?: run {
            fail("Could not find dart file")
            return
        }
        val psiFile = PsiManager.getInstance(project).findFile(vFile) ?: run {
            fail("Could not find psi file")
            return
        }
        val context = ConfigurationContext(psiFile)

        val producer = JasprRunConfigurationProducer()
        val config = createConfig()

        assertFalse(callSetup(producer, config, context))

        dartFile.delete()
    }

    fun testIsConfigurationFromContext() {
        setupJasprProject()
        val producer = JasprRunConfigurationProducer()
        val config = createConfig()
        config.command = "serve"

        val context = createContextForPubspec() ?: run {
            fail("Could not create context for pubspec.yaml")
            return
        }

        val method = producer.javaClass.getDeclaredMethod(
            "isConfigurationFromContext",
            com.intellij.execution.configurations.RunConfiguration::class.java,
            ConfigurationContext::class.java
        )
        method.isAccessible = true
        val result = method.invoke(producer, config, context) as Boolean
        assertTrue(result)
    }
}
