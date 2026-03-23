package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.annotator.JasprComponentContextAnnotator

class JasprComponentContextAnnotatorTest : JasprTestBase() {

    fun testAnnotatorKeysExist() {
        assertNotNull(JasprComponentContextAnnotator.CLIENT_COMPONENT_KEY)
        assertNotNull(JasprComponentContextAnnotator.ISLAND_COMPONENT_KEY)
        assertNotNull(JasprComponentContextAnnotator.SERVER_COMPONENT_KEY)
    }

    fun testClientComponentKeyId() {
        assertEquals(
            "JASPR_CLIENT_COMPONENT",
            JasprComponentContextAnnotator.CLIENT_COMPONENT_KEY.externalName
        )
    }

    fun testIslandComponentKeyId() {
        assertEquals(
            "JASPR_ISLAND_COMPONENT",
            JasprComponentContextAnnotator.ISLAND_COMPONENT_KEY.externalName
        )
    }

    fun testServerComponentKeyId() {
        assertEquals(
            "JASPR_SERVER_COMPONENT",
            JasprComponentContextAnnotator.SERVER_COMPONENT_KEY.externalName
        )
    }

    fun testAnnotatorInstanceCreation() {
        val annotator = JasprComponentContextAnnotator()
        assertNotNull(annotator)
    }
}
