package com.github.nicetyone.jetbrainsjaspr

import com.intellij.codeInsight.completion.CompletionType

class JasprCompletionContributorTest : JasprTestBase() {

    private fun getCompletions(content: String, fileName: String = "test.dart"): List<String> {
        myFixture.configureByText(fileName, content)
        myFixture.complete(CompletionType.BASIC)
        return myFixture.lookupElementStrings ?: emptyList()
    }

    fun testHtmlCompletionsPresent() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              <caret>
            }
        """.trimIndent())

        // Check that at least some Jaspr HTML elements are in the completions
        assertTrue("Should contain HTML element 'div'", "div" in lookupStrings)
        assertTrue("Should contain HTML element 'span'", "span" in lookupStrings)
    }

    fun testHtmlElementsWithDPrefix() {
        setupJasprProject()
        myFixture.configureByText("test.dart", """
            void test() {
              di<caret>
            }
        """.trimIndent())

        val elements = myFixture.completeBasic()
        // With prefix "di", "div" may be auto-inserted (single match),
        // in which case elements is null and the editor text contains "div"
        if (elements == null) {
            assertTrue(
                "div should be auto-inserted",
                myFixture.editor.document.text.contains("div")
            )
        } else {
            val lookupStrings = elements.map { it.lookupString }
            assertTrue("Should contain 'div' with 'di' prefix", "div" in lookupStrings)
        }
    }

    fun testAnnotationCompletions() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              @c<caret>
            }
        """.trimIndent())

        assertTrue("Should contain annotation '@client'", "@client" in lookupStrings || "@css" in lookupStrings)
    }

    fun testBaseClassCompletions() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            class Foo extends State<caret> {}
        """.trimIndent())

        assertTrue("Should contain 'StatelessComponent'", "StatelessComponent" in lookupStrings)
        assertTrue("Should contain 'StatefulComponent'", "StatefulComponent" in lookupStrings)
    }

    fun testPatternCompletions() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              Ro<caret>
            }
        """.trimIndent())

        assertTrue("Should contain 'Router' or 'Route'", "Router" in lookupStrings || "Route" in lookupStrings)
    }

    fun testNoCompletionInNonDartFile() {
        setupJasprProject()
        val lookupStrings = getCompletions("di<caret>", "test.txt")

        assertFalse("Should not contain 'div' in non-dart file", "div" in lookupStrings)
    }

    fun testNoCompletionInNonJasprProject() {
        setupNonJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              di<caret>
            }
        """.trimIndent())

        assertFalse("Should not contain 'div' in non-jaspr project", "div" in lookupStrings)
    }

    fun testAllHtmlElementsPresent() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              <caret>
            }
        """.trimIndent())

        val htmlElements = listOf(
            "div", "p", "h1", "h2", "h3", "h4", "h5", "h6",
            "span", "a", "button", "input", "ul", "li", "form",
            "img", "section", "nav", "header", "footer", "main",
            "article"
        )
        val found = htmlElements.filter { it in lookupStrings }
        assertEquals("Should have all 22 HTML elements, found: $found", 22, found.size)
    }

    fun testAllAnnotationsPresent() {
        setupJasprProject()
        val lookupStrings = getCompletions("""
            void test() {
              <caret>
            }
        """.trimIndent())

        val annotations = listOf("@client", "@island", "@css", "@sync", "@encoder", "@decoder")
        val found = annotations.filter { it in lookupStrings }
        assertEquals("Should have all 6 annotations, found: $found", 6, found.size)
    }
}
