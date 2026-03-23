package com.github.nicetyone.jetbrainsjaspr.codevision

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprConstants
import com.github.nicetyone.jetbrainsjaspr.JasprScopeDetector
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.awt.RelativePoint
import com.jetbrains.lang.dart.psi.DartClass

class JasprScopeCodeVisionProvider : CodeVisionProvider<Unit> {

    companion object {
        const val ID = "jaspr.scope"
    }

    override val id: String = ID
    override val name: String = JasprBundle.message("jaspr.scope.name")
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top

    override fun precomputeOnUiThread(editor: Editor) {}

    override fun computeCodeVision(editor: Editor, uiData: Unit): CodeVisionState {
        val project = editor.project ?: return CodeVisionState.READY_EMPTY
        if (!JasprProjectService.isJasprProject(project)) return CodeVisionState.READY_EMPTY

        return ReadAction.compute<CodeVisionState, Throwable> {
            val document = editor.document
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                ?: return@compute CodeVisionState.READY_EMPTY

            if (!psiFile.name.endsWith(".dart")) return@compute CodeVisionState.READY_EMPTY

            val lenses = ArrayList<Pair<TextRange, CodeVisionEntry>>()
            val dartClasses = PsiTreeUtil.findChildrenOfType(psiFile, DartClass::class.java)

            for (dartClass in dartClasses) {
                if (!isJasprComponent(dartClass)) continue

                val nameElement = dartClass.componentName ?: continue
                val range = nameElement.textRange
                val scope = detectScope(dartClass)

                val entry = createScopeEntry(scope)
                lenses.add(Pair(range, entry))
            }

            CodeVisionState.Ready(lenses)
        }
    }

    override fun handleClick(editor: Editor, textRange: TextRange, entry: CodeVisionEntry) {
        val project = editor.project ?: return

        val psiFile = ReadAction.compute<com.intellij.psi.PsiFile?, Throwable> {
            PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        } ?: return

        val scopeDescription = ReadAction.compute<String, Throwable> {
            val element = psiFile.findElementAt(textRange.startOffset)
            val dartClass = PsiTreeUtil.getParentOfType(element, DartClass::class.java)
            if (dartClass != null) getScopeDescription(detectScope(dartClass)) else ""
        }

        val description = buildString {
            append("<html><body>")
            append("<b>${entry.longPresentation}</b><br><br>")
            if (scopeDescription.isNotEmpty()) {
                append("$scopeDescription<br><br>")
            }
            append("<a href='https://docs.page/schultek/jaspr'>Jaspr Documentation</a>")
            append("</body></html>")
        }

        val point = editor.offsetToXY(textRange.startOffset)

        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(description, null, null, null)
            .setHideOnClickOutside(true)
            .setHideOnKeyOutside(true)
            .createBalloon()
            .show(RelativePoint(editor.contentComponent, point), com.intellij.openapi.ui.popup.Balloon.Position.below)
    }

    private fun isJasprComponent(dartClass: DartClass): Boolean {
        val superClass = dartClass.superClass ?: return false
        val superClassName = superClass.referenceExpression?.text ?: return false
        return superClassName in JasprConstants.COMPONENT_BASE_CLASSES
    }

    private fun detectScope(dartClass: DartClass): ComponentScope {
        val annotations = dartClass.metadataList.mapNotNull {
            it.referenceExpression?.text
        }.toSet()

        if ("client" in annotations) return ComponentScope.CLIENT
        if ("island" in annotations) return ComponentScope.ISLAND

        return when (JasprScopeDetector.detectFileScope(dartClass.containingFile)) {
            JasprScopeDetector.FileScope.SERVER -> ComponentScope.SERVER
            JasprScopeDetector.FileScope.CLIENT -> ComponentScope.CLIENT_ONLY
            JasprScopeDetector.FileScope.SHARED -> ComponentScope.SHARED
        }
    }

    private fun createScopeEntry(scope: ComponentScope): CodeVisionEntry {
        val text = JasprBundle.message(scope.labelKey)
        val tooltip = JasprBundle.message(scope.tooltipKey)

        return ClickableTextCodeVisionEntry(
            text,
            id,
            { _, _ -> },
            null,
            text,
            tooltip
        )
    }

    private fun getScopeDescription(scope: ComponentScope): String {
        return JasprBundle.message(scope.descriptionKey)
    }

    private enum class ComponentScope(val labelKey: String, val tooltipKey: String, val descriptionKey: String) {
        CLIENT("jaspr.scope.client.label", "jaspr.scope.client.tooltip", "jaspr.scope.client.description"),
        CLIENT_ONLY("jaspr.scope.client.only.label", "jaspr.scope.client.only.tooltip", "jaspr.scope.client.only.description"),
        ISLAND("jaspr.scope.island.label", "jaspr.scope.island.tooltip", "jaspr.scope.island.description"),
        SERVER("jaspr.scope.server.label", "jaspr.scope.server.tooltip", "jaspr.scope.server.description"),
        SHARED("jaspr.scope.shared.label", "jaspr.scope.shared.tooltip", "jaspr.scope.shared.description")
    }
}
