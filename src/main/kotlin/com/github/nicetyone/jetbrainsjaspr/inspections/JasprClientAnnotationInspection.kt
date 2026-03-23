package com.github.nicetyone.jetbrainsjaspr.inspections

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartClass

class JasprClientAnnotationInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        if (!JasprProjectService.isJasprProject(project)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                var clientClassCount = 0

                file.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        super.visitElement(element)

                        if (element !is DartClass) return

                        val annotations = element.metadataList
                        val hasClient = annotations.any { it.referenceExpression?.text == "client" }

                        if (!hasClient) return

                        clientClassCount++
                        if (clientClassCount > 1) {
                            val nameElement = element.componentName ?: return
                            holder.registerProblem(
                                nameElement,
                                JasprBundle.message("jaspr.inspection.client.annotation.multiple"),
                                ProblemHighlightType.WARNING
                            )
                        }
                    }
                })
            }
        }
    }
}
