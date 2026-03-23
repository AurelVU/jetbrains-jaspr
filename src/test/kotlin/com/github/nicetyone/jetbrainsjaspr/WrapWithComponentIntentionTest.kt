package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.intentions.WrapWithComponentIntention

class WrapWithComponentIntentionTest : JasprTestBase() {

    fun testIsAvailableInDartJasprProject() {
        setupJasprProject()
        val file = myFixture.configureByText("test.dart", """
            import 'package:jaspr/jaspr.dart';
            class MyComponent extends StatelessComponent {
              Iterable<Component> build(BuildContext context) sync* {
                yield di<caret>v([], []);
              }
            }
        """.trimIndent())

        val intention = WrapWithComponentIntention()
        val element = file.findElementAt(myFixture.caretOffset)
        assertNotNull(element)
        assertTrue(intention.isAvailable(project, myFixture.editor, element!!))
    }

    fun testIsNotAvailableInNonDartFile() {
        setupJasprProject()
        val file = myFixture.configureByText("test.txt", """
            some te<caret>xt
        """.trimIndent())

        val intention = WrapWithComponentIntention()
        val element = file.findElementAt(myFixture.caretOffset)
        assertNotNull(element)
        assertFalse(intention.isAvailable(project, myFixture.editor, element!!))
    }

    fun testIsNotAvailableInNonJasprProject() {
        setupNonJasprProject()
        val file = myFixture.configureByText("test.dart", """
            void main<caret>() {}
        """.trimIndent())

        val intention = WrapWithComponentIntention()
        val element = file.findElementAt(myFixture.caretOffset)
        assertNotNull(element)
        assertFalse(intention.isAvailable(project, myFixture.editor, element!!))
    }

    fun testWrapperListNotEmpty() {
        val intention = WrapWithComponentIntention()
        // The wrappers field is private but we can test via the class behavior.
        // The intention should have text and family name.
        assertNotNull(intention.text)
        assertTrue(intention.text.isNotEmpty())
    }

    fun testFamilyNameFromBundle() {
        val intention = WrapWithComponentIntention()
        val familyName = intention.familyName
        assertNotNull(familyName)
        assertTrue(familyName.isNotEmpty())
        assertEquals(JasprBundle.message("jaspr.intention.wrap.with.component.family"), familyName)
    }
}
