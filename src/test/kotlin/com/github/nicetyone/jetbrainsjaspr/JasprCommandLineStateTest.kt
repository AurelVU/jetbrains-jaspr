package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprCommandLineState
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprConfigurationType
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfiguration
import com.github.nicetyone.jetbrainsjaspr.runconfig.JasprRunConfigurationFactory
import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder

class JasprCommandLineStateTest : JasprTestBase() {

    private fun createState(configure: JasprRunConfiguration.() -> Unit): JasprCommandLineState {
        val type = JasprConfigurationType()
        val factory = JasprRunConfigurationFactory(type)
        val config = JasprRunConfiguration(project, factory, "Test")
        config.configure()

        val executor = DefaultRunExecutor.getRunExecutorInstance()
        val env = ExecutionEnvironmentBuilder.create(executor, config).build()
        return JasprCommandLineState(config, env)
    }

    fun testServeWithDefaultPort() {
        val state = createState {
            command = "serve"
            port = "8080"
        }
        val cmd = state.buildCommandLine()
        val params = cmd.parametersList.list

        assertTrue(params.contains("serve"))
        assertFalse("--port" in params)
    }

    fun testServeWithCustomPort() {
        val state = createState {
            command = "serve"
            port = "3000"
        }
        val cmd = state.buildCommandLine()
        val params = cmd.parametersList.list

        assertTrue(params.contains("serve"))
        val portIndex = params.indexOf("--port")
        assertTrue(portIndex >= 0)
        assertEquals("3000", params[portIndex + 1])
    }

    fun testBuildIgnoresPort() {
        val state = createState {
            command = "build"
            port = "3000"
        }
        val cmd = state.buildCommandLine()
        val params = cmd.parametersList.list

        assertTrue(params.contains("build"))
        assertFalse("--port" in params)
    }

    fun testNoSsrEnabled() {
        val state = createState {
            command = "serve"
            noSsr = true
        }
        val cmd = state.buildCommandLine()
        assertTrue("--no-ssr" in cmd.parametersList.list)
    }

    fun testNoSsrDisabled() {
        val state = createState {
            command = "serve"
            noSsr = false
        }
        val cmd = state.buildCommandLine()
        assertFalse("--no-ssr" in cmd.parametersList.list)
    }

    fun testTargetPresent() {
        val state = createState {
            command = "serve"
            target = "lib/app.dart"
        }
        val cmd = state.buildCommandLine()
        val params = cmd.parametersList.list

        val targetIndex = params.indexOf("--target")
        assertTrue(targetIndex >= 0)
        assertEquals("lib/app.dart", params[targetIndex + 1])
    }

    fun testTargetAbsent() {
        val state = createState {
            command = "serve"
            target = ""
        }
        val cmd = state.buildCommandLine()
        assertFalse("--target" in cmd.parametersList.list)
    }

    fun testAdditionalArgs() {
        val state = createState {
            command = "serve"
            additionalArgs = "--verbose --release"
        }
        val cmd = state.buildCommandLine()
        val params = cmd.parametersList.list

        assertTrue("--verbose" in params)
        assertTrue("--release" in params)
    }

    fun testDefaultExecutable() {
        JasprSettings.getInstance(project).jasprCliPath = ""
        val state = createState { command = "serve" }
        val cmd = state.buildCommandLine()

        assertEquals("dart", cmd.exePath)
        val params = cmd.parametersList.list
        assertTrue(params.contains("run"))
        assertTrue(params.contains("jaspr_cli:jaspr"))
    }

    fun testCustomCliPath() {
        JasprSettings.getInstance(project).jasprCliPath = "/usr/local/bin/jaspr"
        val state = createState { command = "serve" }
        val cmd = state.buildCommandLine()

        assertEquals("/usr/local/bin/jaspr", cmd.exePath)
        val params = cmd.parametersList.list
        assertFalse(params.contains("run"))
        assertFalse(params.contains("jaspr_cli:jaspr"))
    }
}
