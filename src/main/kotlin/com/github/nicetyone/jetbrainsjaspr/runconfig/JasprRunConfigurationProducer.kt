package com.github.nicetyone.jetbrainsjaspr.runconfig

import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class JasprRunConfigurationProducer : LazyRunConfigurationProducer<JasprRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        JasprConfigurationType.getInstance().configurationFactories[0]

    override fun setupConfigurationFromContext(
        configuration: JasprRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val project = context.project ?: return false
        if (!JasprProjectService.isJasprProject(project)) return false

        val file = context.location?.virtualFile ?: return false
        if (file.name != "pubspec.yaml") return false

        configuration.name = "jaspr serve"
        configuration.command = "serve"
        return true
    }

    override fun isConfigurationFromContext(
        configuration: JasprRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val project = context.project ?: return false
        if (!JasprProjectService.isJasprProject(project)) return false

        val file = context.location?.virtualFile ?: return false
        return file.name == "pubspec.yaml" && configuration.command == "serve"
    }
}
