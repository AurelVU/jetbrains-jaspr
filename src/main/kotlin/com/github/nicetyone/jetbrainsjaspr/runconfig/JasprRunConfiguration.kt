package com.github.nicetyone.jetbrainsjaspr.runconfig

import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import javax.swing.Icon

class JasprRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, name) {

    var command: String = "serve"
    var port: String = "8080"
    var noSsr: Boolean = false
    var target: String = ""
    var additionalArgs: String = ""

    override fun getIcon(): Icon = JasprIcons.JASPR

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        JasprRunConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        JasprCommandLineState(this, environment)

    override fun checkConfiguration() {
        if (command.isBlank()) {
            throw RuntimeConfigurationError("Jaspr command must not be empty")
        }

        val portStr = port.trim()
        if (portStr.isNotEmpty()) {
            val portNum = portStr.toIntOrNull()
                ?: throw RuntimeConfigurationError("Port must be a valid number")
            if (portNum !in 1..65535) {
                throw RuntimeConfigurationError("Port must be between 1 and 65535")
            }
        }
    }

    override fun readExternal(element: org.jdom.Element) {
        super.readExternal(element)
        command = element.getAttributeValue("jaspr-command") ?: "serve"
        port = element.getAttributeValue("jaspr-port") ?: "8080"
        noSsr = element.getAttributeValue("jaspr-no-ssr")?.toBoolean() ?: false
        target = element.getAttributeValue("jaspr-target") ?: ""
        additionalArgs = element.getAttributeValue("jaspr-additional-args") ?: ""
    }

    override fun writeExternal(element: org.jdom.Element) {
        super.writeExternal(element)
        element.setAttribute("jaspr-command", command)
        element.setAttribute("jaspr-port", port)
        element.setAttribute("jaspr-no-ssr", noSsr.toString())
        element.setAttribute("jaspr-target", target)
        element.setAttribute("jaspr-additional-args", additionalArgs)
    }
}
