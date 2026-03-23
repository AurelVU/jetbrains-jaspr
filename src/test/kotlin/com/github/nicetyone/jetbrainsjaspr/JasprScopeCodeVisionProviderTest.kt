package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.codevision.JasprScopeCodeVisionProvider
import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind

class JasprScopeCodeVisionProviderTest : JasprTestBase() {

    fun testProviderId() {
        val provider = JasprScopeCodeVisionProvider()
        assertEquals("jaspr.scope", provider.id)
    }

    fun testProviderName() {
        val provider = JasprScopeCodeVisionProvider()
        assertNotNull(provider.name)
        assertTrue(provider.name.isNotEmpty())
    }

    fun testDefaultAnchor() {
        val provider = JasprScopeCodeVisionProvider()
        assertEquals(CodeVisionAnchorKind.Top, provider.defaultAnchor)
    }

    fun testReadyEmptyForNonJasprProject() {
        setupNonJasprProject()
        val provider = JasprScopeCodeVisionProvider()

        myFixture.configureByText("test.dart", """
            class MyComponent extends StatelessComponent {}
        """.trimIndent())

        val result = provider.computeCodeVision(myFixture.editor, Unit)
        assertEquals(
            "Non-jaspr project should return READY_EMPTY",
            com.intellij.codeInsight.codeVision.CodeVisionState.READY_EMPTY,
            result
        )
    }

    fun testReadyEmptyForNonDartFile() {
        setupJasprProject()
        val provider = JasprScopeCodeVisionProvider()

        myFixture.configureByText("test.txt", """
            some text
        """.trimIndent())

        val result = provider.computeCodeVision(myFixture.editor, Unit)
        assertEquals(
            "Non-dart file should return READY_EMPTY",
            com.intellij.codeInsight.codeVision.CodeVisionState.READY_EMPTY,
            result
        )
    }
}
