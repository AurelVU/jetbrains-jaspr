package com.github.nicetyone.jetbrainsjaspr.intentions

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement

class WrapWithComponentIntention : PsiElementBaseIntentionAction(), IntentionAction {

    private val wrappers = listOf("div", "span", "section", "nav", "header", "footer", "main_", "article", "p")

    override fun getText(): String = JasprBundle.message("jaspr.intention.wrap.with.component")

    override fun getFamilyName(): String = JasprBundle.message("jaspr.intention.wrap.with.component.family")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!JasprProjectService.isJasprProject(project)) return false
        val file = element.containingFile ?: return false
        return file.name.endsWith(".dart")
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        editor ?: return

        val document = editor.document
        val selectionModel = editor.selectionModel

        if (!selectionModel.hasSelection()) {
            // Try to select the current expression
            val start = element.textRange.startOffset
            val end = element.textRange.endOffset
            selectionModel.setSelection(start, end)
        }

        val selectedText = selectionModel.selectedText ?: return
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd

        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(wrappers)
            .setTitle("Wrap with")
            .setItemChosenCallback { wrapper ->
                val wrapped = "$wrapper([$selectedText])"
                com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                    document.replaceString(startOffset, endOffset, wrapped)
                }
            }
            .createPopup()
            .showInBestPositionFor(editor)
    }
}
