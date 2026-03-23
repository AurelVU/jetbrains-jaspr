package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.inspections.JasprClientAnnotationInspection

class JasprClientAnnotationInspectionTest : JasprTestBase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(JasprClientAnnotationInspection())
    }

    fun testSingleClientNoWarning() {
        setupJasprProject()
        myFixture.configureByText("test.dart", """
            import 'package:jaspr/jaspr.dart';

            @client
            class MyComponent extends StatelessComponent {
              @override
              Iterable<Component> build(BuildContext context) sync* {
                yield div([], []);
              }
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val clientWarnings = highlights.filter {
            it.description?.contains("Only one @client") == true
        }
        assertTrue("Single @client should produce no warnings", clientWarnings.isEmpty())
    }

    fun testMultipleClientsWarning() {
        setupJasprProject()
        myFixture.configureByText("test.dart", """
            import 'package:jaspr/jaspr.dart';

            @client
            class FirstComponent extends StatelessComponent {
              @override
              Iterable<Component> build(BuildContext context) sync* {
                yield div([], []);
              }
            }

            @client
            class SecondComponent extends StatelessComponent {
              @override
              Iterable<Component> build(BuildContext context) sync* {
                yield span([], []);
              }
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val clientWarnings = highlights.filter {
            it.description?.contains("Only one @client") == true
        }
        assertFalse("Multiple @client should produce a warning", clientWarnings.isEmpty())
    }

    fun testNoClientAnnotationNoWarning() {
        setupJasprProject()
        myFixture.configureByText("test.dart", """
            import 'package:jaspr/jaspr.dart';

            class MyComponent extends StatelessComponent {
              @override
              Iterable<Component> build(BuildContext context) sync* {
                yield div([], []);
              }
            }
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val clientWarnings = highlights.filter {
            it.description?.contains("Only one @client") == true
        }
        assertTrue("No @client should produce no warnings", clientWarnings.isEmpty())
    }

    fun testDisabledInNonJasprProject() {
        setupNonJasprProject()
        myFixture.configureByText("test.dart", """
            @client
            class FirstComponent {}

            @client
            class SecondComponent {}
        """.trimIndent())

        val highlights = myFixture.doHighlighting()
        val clientWarnings = highlights.filter {
            it.description?.contains("Only one @client") == true
        }
        assertTrue("Non-jaspr project should produce no warnings", clientWarnings.isEmpty())
    }
}
