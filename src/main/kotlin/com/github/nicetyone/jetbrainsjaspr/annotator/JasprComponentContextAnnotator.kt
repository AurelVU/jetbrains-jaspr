package com.github.nicetyone.jetbrainsjaspr.annotator

import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponentName

class JasprComponentContextAnnotator : Annotator {

    companion object {
        val CLIENT_COMPONENT_KEY = TextAttributesKey.createTextAttributesKey(
            "JASPR_CLIENT_COMPONENT",
            com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_NAME
        )
        val ISLAND_COMPONENT_KEY = TextAttributesKey.createTextAttributesKey(
            "JASPR_ISLAND_COMPONENT",
            com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_NAME
        )
        val SERVER_COMPONENT_KEY = TextAttributesKey.createTextAttributesKey(
            "JASPR_SERVER_COMPONENT",
            com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_NAME
        )
    }

    private val componentBaseClasses = setOf(
        "StatelessComponent",
        "StatefulComponent",
        "InheritedComponent"
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is DartComponentName) return

        val project = element.project
        if (!JasprProjectService.isJasprProject(project)) return

        val dartClass = element.parent as? DartClass ?: return
        if (!isJasprComponent(dartClass)) return

        val context = detectComponentContext(dartClass)
        val (key, message) = when (context) {
            ComponentContext.CLIENT -> CLIENT_COMPONENT_KEY to "Client Component"
            ComponentContext.ISLAND -> ISLAND_COMPONENT_KEY to "Island Component"
            ComponentContext.SERVER -> SERVER_COMPONENT_KEY to "Server Component"
            ComponentContext.SHARED -> return
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .textAttributes(key)
            .tooltip(message)
            .create()
    }

    private fun isJasprComponent(dartClass: DartClass): Boolean {
        val superClass = dartClass.superClass ?: return false
        val superClassName = superClass.referenceExpression?.text ?: return false
        return superClassName in componentBaseClasses
    }

    private fun detectComponentContext(dartClass: DartClass): ComponentContext {
        val annotations = getAnnotations(dartClass)

        if ("client" in annotations) return ComponentContext.CLIENT
        if ("island" in annotations) return ComponentContext.ISLAND

        val fileText = dartClass.containingFile?.text ?: return ComponentContext.SHARED

        return when {
            fileText.contains("package:jaspr/server.dart") -> ComponentContext.SERVER
            fileText.contains("package:jaspr/browser.dart") -> ComponentContext.CLIENT
            else -> ComponentContext.SHARED
        }
    }

    private fun getAnnotations(dartClass: DartClass): Set<String> {
        val metadata = dartClass.metadataList
        return metadata.mapNotNull { it.referenceExpression?.text }.toSet()
    }

    private enum class ComponentContext {
        CLIENT, ISLAND, SERVER, SHARED
    }
}
