package com.github.nicetyone.jetbrainsjaspr.runconfig

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil

class JasprConfigurationType : ConfigurationTypeBase(
    ID,
    JasprBundle.message("jaspr.run.configuration.type.name"),
    JasprBundle.message("jaspr.run.configuration.type.description"),
    JasprIcons.JASPR
) {
    init {
        addFactory(JasprRunConfigurationFactory(this))
    }

    companion object {
        const val ID = "JasprRunConfiguration"

        fun getInstance(): JasprConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(JasprConfigurationType::class.java)
    }
}
