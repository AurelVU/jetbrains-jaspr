package com.github.nicetyone.jetbrainsjaspr

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartImportStatement

object JasprScopeDetector {

    enum class FileScope {
        SERVER, CLIENT, SHARED
    }

    fun detectFileScope(psiFile: PsiFile?): FileScope {
        if (psiFile == null) return FileScope.SHARED

        val imports = PsiTreeUtil.findChildrenOfType(psiFile, DartImportStatement::class.java)
        for (importStatement in imports) {
            val uri = importStatement.uriString
            when {
                uri == "package:jaspr/server.dart" -> return FileScope.SERVER
                uri == "package:jaspr/browser.dart" -> return FileScope.CLIENT
            }
        }

        return FileScope.SHARED
    }
}
