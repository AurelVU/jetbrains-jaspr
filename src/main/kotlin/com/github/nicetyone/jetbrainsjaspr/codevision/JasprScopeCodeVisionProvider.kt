package com.github.nicetyone.jetbrainsjaspr.codevision

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.codeInsight.codeVision.*
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass

class JasprScopeCodeVisionProvider : CodeVisionProvider<Unit> {

    companion object {
        const val ID = "jaspr.scope"

        private val COMPONENT_BASE_CLASSES = setOf(
            "StatelessComponent",
            "StatefulComponent",
            "InheritedComponent"
        )
    }

    override val id: String = ID
    override val name: String = JasprBundle.message("jaspr.scope.name")
    override val relativeOrderings: List<CodeVisionRelativeOrdering> = emptyList()
    override val defaultAnchor: CodeVisionAnchorKind = CodeVisionAnchorKind.Top

    override fun precomputeOnUiThread(editor: Editor) {}

    override fun computeCodeVision(editor: Editor, uiData: Unit): CodeVisionState {
        val project = editor.project ?: return CodeVisionState.READY_EMPTY
        if (!JasprProjectService.isJasprProject(project)) return CodeVisionState.READY_EMPTY

        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
            ?: return CodeVisionState.READY_EMPTY

        if (!psiFile.name.endsWith(".dart")) return CodeVisionState.READY_EMPTY

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

        return CodeVisionState.Ready(lenses)
    }

    override fun handleClick(editor: Editor, textRange: TextRange, entry: CodeVisionEntry) {}

    private fun isJasprComponent(dartClass: DartClass): Boolean {
        val superClass = dartClass.superClass ?: return false
        val superClassName = superClass.referenceExpression?.text ?: return false
        return superClassName in COMPONENT_BASE_CLASSES
    }

    private fun detectScope(dartClass: DartClass): ComponentScope {
        val annotations = dartClass.metadataList.mapNotNull {
            it.referenceExpression?.text
        }.toSet()

        if ("client" in annotations) return ComponentScope.CLIENT
        if ("island" in annotations) return ComponentScope.ISLAND

        val fileText = dartClass.containingFile?.text ?: return ComponentScope.SHARED

        return when {
            fileText.contains("package:jaspr/server.dart") -> ComponentScope.SERVER
            fileText.contains("package:jaspr/browser.dart") -> ComponentScope.CLIENT_ONLY
            else -> ComponentScope.SHARED
        }
    }

    private fun createScopeEntry(scope: ComponentScope): CodeVisionEntry {
        val (text, icon) = when (scope) {
            ComponentScope.CLIENT -> {
                val label = "${JasprBundle.message("jaspr.scope.server")}  \u00b7  ${JasprBundle.message("jaspr.scope.client")}"
                label to AllIcons.Nodes.PpWeb
            }
            ComponentScope.CLIENT_ONLY -> {
                JasprBundle.message("jaspr.scope.client") to AllIcons.Nodes.PpWeb
            }
            ComponentScope.ISLAND -> {
                val label = "${JasprBundle.message("jaspr.scope.server")}  \u00b7  ${JasprBundle.message("jaspr.scope.island")}"
                label to AllIcons.Nodes.PpJdk
            }
            ComponentScope.SERVER -> {
                JasprBundle.message("jaspr.scope.server") to AllIcons.Webreferences.Server
            }
            ComponentScope.SHARED -> {
                JasprBundle.message("jaspr.scope.shared") to AllIcons.Nodes.Shared
            }
        }

        return ClickableTextCodeVisionEntry(
            text,
            id,
            { _, _ -> },
            icon,
            text,
            JasprBundle.message("jaspr.scope.description")
        )
    }

    private enum class ComponentScope {
        CLIENT, CLIENT_ONLY, ISLAND, SERVER, SHARED
    }
}
