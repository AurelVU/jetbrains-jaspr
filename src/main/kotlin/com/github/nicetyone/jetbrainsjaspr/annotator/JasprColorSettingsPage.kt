package com.github.nicetyone.jetbrainsjaspr.annotator

import com.github.nicetyone.jetbrainsjaspr.JasprBundle
import com.github.nicetyone.jetbrainsjaspr.JasprIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class JasprColorSettingsPage : ColorSettingsPage {

    private val descriptors = arrayOf(
        AttributesDescriptor(
            JasprBundle.message("jaspr.color.client.component"),
            JasprComponentContextAnnotator.CLIENT_COMPONENT_KEY
        ),
        AttributesDescriptor(
            JasprBundle.message("jaspr.color.island.component"),
            JasprComponentContextAnnotator.ISLAND_COMPONENT_KEY
        ),
        AttributesDescriptor(
            JasprBundle.message("jaspr.color.server.component"),
            JasprComponentContextAnnotator.SERVER_COMPONENT_KEY
        ),
    )

    override fun getIcon(): Icon = JasprIcons.JASPR

    override fun getHighlighter() =
        com.intellij.openapi.fileTypes.SyntaxHighlighterFactory.getSyntaxHighlighter(
            com.jetbrains.lang.dart.DartLanguage.INSTANCE, null, null
        )

    override fun getDemoText(): String = """
        import 'package:jaspr/jaspr.dart';

        @client
        class <clientComp>CounterButton</clientComp> extends StatefulComponent {
          const CounterButton({super.key});
        }

        @island
        class <islandComp>InteractiveWidget</islandComp> extends StatelessComponent {
          const InteractiveWidget({super.key});
        }

        // Server-only component
        class <serverComp>ServerLayout</serverComp> extends StatelessComponent {
          const ServerLayout({super.key});
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = mapOf(
        "clientComp" to JasprComponentContextAnnotator.CLIENT_COMPONENT_KEY,
        "islandComp" to JasprComponentContextAnnotator.ISLAND_COMPONENT_KEY,
        "serverComp" to JasprComponentContextAnnotator.SERVER_COMPONENT_KEY,
    )

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = JasprBundle.message("jaspr.color.settings.title")
}
