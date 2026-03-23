package com.github.nicetyone.jetbrainsjaspr.settings

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.services.JasprSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class JasprSettingsConfigurable(private val project: Project) : SearchableConfigurable {

    private var pathField: TextFieldWithBrowseButton? = null

    override fun getId(): String = "jaspr.settings"

    override fun getDisplayName(): String = JasprBundle.message("jaspr.settings.title")

    @Suppress("DialogTitleCapitalization")
    override fun createComponent(): JComponent {
        pathField = TextFieldWithBrowseButton().apply {
            @Suppress("DEPRECATION")
            addBrowseFolderListener(
                JasprBundle.message("jaspr.settings.cli.path.title"),
                JasprBundle.message("jaspr.settings.cli.path.description"),
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }

        return panel {
            group("Jaspr SDK") {
                row(JasprBundle.message("jaspr.settings.cli.path")) {
                    cell(pathField!!)
                        .resizableColumn()
                        .comment("Leave empty to use <code>dart run jaspr_cli:jaspr</code> from the project directory")
                }
            }
        }
    }

    override fun isModified(): Boolean {
        val settings = JasprSettings.getInstance(project)
        return pathField?.text != settings.jasprCliPath
    }

    override fun apply() {
        val settings = JasprSettings.getInstance(project)
        settings.jasprCliPath = pathField?.text ?: ""
    }

    override fun reset() {
        val settings = JasprSettings.getInstance(project)
        pathField?.text = settings.jasprCliPath
    }

    override fun disposeUIResources() {
        pathField = null
    }
}
