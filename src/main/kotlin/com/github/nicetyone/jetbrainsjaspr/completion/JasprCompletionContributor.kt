package com.github.nicetyone.jetbrainsjaspr.completion

import com.github.nicetyone.jetbrainsjaspr.JasprConstants
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class JasprCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            JasprCompletionProvider()
        )
    }

    private class JasprCompletionProvider : CompletionProvider<CompletionParameters>() {

        private val htmlElements = listOf(
            "div" to "div([], [])",
            "p" to "p([], [])",
            "h1" to "h1([], [])",
            "h2" to "h2([], [])",
            "h3" to "h3([], [])",
            "h4" to "h4([], [])",
            "h5" to "h5([], [])",
            "h6" to "h6([], [])",
            "span" to "span([], [])",
            "a" to "a(href: '', [], [])",
            "button" to "button([], [])",
            "input" to "input([])",
            "ul" to "ul([], [])",
            "li" to "li([], [])",
            "form" to "form([], [])",
            "img" to "img(src: '', alt: '')",
            "section" to "section([], [])",
            "nav" to "nav([], [])",
            "header" to "header([], [])",
            "footer" to "footer([], [])",
            "main" to "main_([], [])",
            "article" to "article([], [])",
        )

        private val annotations = listOf(
            "@client" to "Mark component for client-side rendering",
            "@island" to "Mark component as an interactive island",
            "@css" to "Define component CSS styles",
            "@sync" to "Synchronize state between server and client",
            "@encoder" to "Custom encoder for serialization",
            "@decoder" to "Custom decoder for deserialization",
        )

        private val baseClassDescriptions = mapOf(
            "StatelessComponent" to "Base class for stateless Jaspr components",
            "StatefulComponent" to "Base class for stateful Jaspr components",
            "InheritedComponent" to "Base class for inherited Jaspr components",
        )

        private val patterns = listOf(
            "Styles" to "Styles(\n  \n)",
            "events" to "events({})",
            "Router" to "Router(routes: [\n  \n])",
            "Route" to "Route(path: '/', builder: (context, state) => )",
        )

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val project = parameters.editor.project ?: return
            if (!JasprProjectService.isJasprProject(project)) return

            val file = parameters.originalFile
            if (!file.name.endsWith(".dart")) return

            for ((name, template) in htmlElements) {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                            .withIcon(JasprIcons.JASPR)
                            .withTypeText("Jaspr HTML")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val offset = ctx.startOffset
                                editor.document.replaceString(offset, ctx.tailOffset, template)
                                editor.caretModel.moveToOffset(offset + template.indexOf("[]") + 1)
                            },
                        -1.0
                    )
                )
            }

            for ((name, description) in annotations) {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                            .withIcon(JasprIcons.JASPR)
                            .withTypeText("Jaspr Annotation")
                            .withTailText(" — $description", true),
                        -1.0
                    )
                )
            }

            for (name in JasprConstants.COMPONENT_BASE_CLASSES) {
                val description = baseClassDescriptions[name] ?: ""
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                            .withIcon(JasprIcons.JASPR)
                            .withTypeText("Jaspr Component")
                            .withTailText(" — $description", true),
                        -1.0
                    )
                )
            }

            for ((name, template) in patterns) {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(name)
                            .withIcon(JasprIcons.JASPR)
                            .withTypeText("Jaspr Pattern")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val offset = ctx.startOffset
                                editor.document.replaceString(offset, ctx.tailOffset, template)
                            },
                        -1.0
                    )
                )
            }
        }
    }
}
