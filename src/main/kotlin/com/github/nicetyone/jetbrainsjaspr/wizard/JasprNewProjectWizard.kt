package com.github.nicetyone.jetbrainsjaspr.wizard

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizard
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep
import javax.swing.Icon

class JasprNewProjectWizard : GeneratorNewProjectWizard {
    override val id: String = "jaspr"
    override val name: String = "Jaspr"
    override val icon: Icon = JasprIcons.JASPR
    override val description: String = JasprBundle.message("jaspr.new.project.description")

    override fun createStep(context: WizardContext): NewProjectWizardStep =
        NewProjectWizardChainStep(RootNewProjectWizardStep(context))
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::JasprNewProjectWizardStep)
}
