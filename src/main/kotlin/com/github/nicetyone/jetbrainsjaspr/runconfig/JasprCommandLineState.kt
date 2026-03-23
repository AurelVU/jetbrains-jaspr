package com.github.nicetyone.jetbrainsjaspr.runconfig

import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class JasprCommandLineState(
    private val config: JasprRunConfiguration,
    environment: ExecutionEnvironment
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine = buildCommandLine()
        val handler = ColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        return handler
    }

    internal fun buildCommandLine(): GeneralCommandLine {
        val workDir = config.project.basePath ?: "."

        val args = mutableListOf<String>()
        args.add(config.command)

        if (config.command == "serve") {
            val port = config.port.trim()
            if (port.isNotEmpty() && port != "8080") {
                args.add("--port")
                args.add(port)
            }
        }

        if (config.noSsr) {
            args.add("--no-ssr")
        }

        val target = config.target.trim()
        if (target.isNotEmpty()) {
            args.add("--target")
            args.add(target)
        }

        val additional = config.additionalArgs.trim()
        if (additional.isNotEmpty()) {
            args.addAll(additional.split("\\s+".toRegex()))
        }

        val jasprCliPath = JasprSettings.getInstance(config.project).jasprCliPath

        val commandLine = if (jasprCliPath.isNotEmpty()) {
            GeneralCommandLine(jasprCliPath).withParameters(args)
        } else {
            GeneralCommandLine("dart", "run", "jaspr_cli:jaspr").withParameters(args)
        }

        return commandLine
            .withWorkDirectory(workDir)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
    }
}
