package com.github.nicetyone.jetbrainsjaspr.actions

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.github.nicetyone.jetbrainsjaspr.templates.JasprFileTemplateGroupFactory
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class NewJasprPageAction : CreateFileFromTemplateAction(
    JasprBundle.message("jaspr.new.page.action"),
    JasprBundle.message("jaspr.new.page.action.description"),
    JasprIcons.JASPR
), DumbAware {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(JasprBundle.message("jaspr.new.page.action"))
            .addKind("Page", JasprIcons.JASPR, JasprFileTemplateGroupFactory.PAGE)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        JasprBundle.message("jaspr.new.page.action")

    override fun update(e: com.intellij.openapi.actionSystem.AnActionEvent) {
        super.update(e)
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null && JasprProjectService.isJasprProject(project)
    }
}
