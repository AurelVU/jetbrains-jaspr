package com.github.nicetyone.jetbrainsjaspr.runconfig

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class JasprRunConfigurationEditor(
    @Suppress("unused") private val project: Project
) : SettingsEditor<JasprRunConfiguration>() {

    private val commandCombo = ComboBox(DefaultComboBoxModel(arrayOf("serve", "build", "clean")))
    private val portField = JBTextField()
    private val noSsrCheckbox = JBCheckBox(JasprBundle.message("jaspr.run.configuration.editor.no.ssr"))
    private val targetField = JBTextField()
    private val additionalArgsField = JBTextField()

    override fun createEditor(): JComponent = panel {
        row(JasprBundle.message("jaspr.run.configuration.editor.command")) {
            cell(commandCombo)
        }
        row(JasprBundle.message("jaspr.run.configuration.editor.port")) {
            cell(portField)
        }
        row("") {
            cell(noSsrCheckbox)
        }
        row(JasprBundle.message("jaspr.run.configuration.editor.target")) {
            cell(targetField)
        }
        row(JasprBundle.message("jaspr.run.configuration.editor.additional.args")) {
            cell(additionalArgsField)
        }
    }

    override fun resetEditorFrom(config: JasprRunConfiguration) {
        commandCombo.selectedItem = config.command
        portField.text = config.port
        noSsrCheckbox.isSelected = config.noSsr
        targetField.text = config.target
        additionalArgsField.text = config.additionalArgs
    }

    override fun applyEditorTo(config: JasprRunConfiguration) {
        config.command = commandCombo.selectedItem as? String ?: "serve"
        config.port = portField.text
        config.noSsr = noSsrCheckbox.isSelected
        config.target = targetField.text
        config.additionalArgs = additionalArgsField.text
    }
}
