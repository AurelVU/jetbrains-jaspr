package com.github.nicetyone.jetbrainsjaspr

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JasprBundleTest : BasePlatformTestCase() {

    fun testKnownKeysReturnNonEmptyStrings() {
        val keys = listOf(
            "jaspr.run.configuration.type.name",
            "jaspr.run.configuration.type.description",
            "jaspr.intention.wrap.with.component",
            "jaspr.intention.wrap.with.component.family",
            "jaspr.inspection.client.annotation.multiple",
            "jaspr.scope.name",
            "jaspr.scope.description"
        )
        for (key in keys) {
            val value = JasprBundle.message(key)
            assertTrue("Key '$key' should return non-empty string", value.isNotEmpty())
        }
    }

    fun testScopeKeysExist() {
        val scopeKeys = listOf(
            "jaspr.scope.server",
            "jaspr.scope.client",
            "jaspr.scope.island",
            "jaspr.scope.shared"
        )
        for (key in scopeKeys) {
            val value = JasprBundle.message(key)
            assertTrue("Scope key '$key' should return non-empty string", value.isNotEmpty())
        }
    }
}
