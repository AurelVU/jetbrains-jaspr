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
            "jaspr.scope.name"
        )
        for (key in keys) {
            val value = JasprBundle.message(key)
            assertTrue("Key '$key' should return non-empty string", value.isNotEmpty())
        }
    }

    fun testScopeKeysExist() {
        val scopeKeys = listOf(
            "jaspr.scope.server.label",
            "jaspr.scope.server.tooltip",
            "jaspr.scope.server.description",
            "jaspr.scope.client.label",
            "jaspr.scope.client.tooltip",
            "jaspr.scope.client.description",
            "jaspr.scope.island.label",
            "jaspr.scope.island.tooltip",
            "jaspr.scope.island.description",
            "jaspr.scope.shared.label",
            "jaspr.scope.shared.tooltip",
            "jaspr.scope.shared.description",
            "jaspr.scope.client.only.label",
            "jaspr.scope.client.only.tooltip",
            "jaspr.scope.client.only.description"
        )
        for (key in scopeKeys) {
            val value = JasprBundle.message(key)
            assertTrue("Scope key '$key' should return non-empty string", value.isNotEmpty())
        }
    }
}
