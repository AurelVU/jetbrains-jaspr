package com.github.nicetyone.jetbrainsjaspr

import com.github.nicetyone.jetbrainsjaspr.services.JasprProjectService

class JasprProjectServiceTest : JasprTestBase() {

    fun testIsJasprProjectWithJasprDependency() {
        setupJasprProject()
        assertTrue(JasprProjectService.isJasprProject(project))
    }

    fun testIsJasprProjectWithJasprBuilder() {
        setupJasprBuilderProject()
        assertTrue(JasprProjectService.isJasprProject(project))
    }

    fun testIsNotJasprProjectWithoutJaspr() {
        setupNonJasprProject()
        assertFalse(JasprProjectService.isJasprProject(project))
    }

    fun testIsNotJasprProjectWithoutPubspec() {
        assertFalse(JasprProjectService.isJasprProject(project))
    }

    fun testFindPubspecFileFound() {
        setupJasprProject()
        val pubspec = JasprProjectService.getInstance(project).findPubspecFile()
        assertNotNull(pubspec)
        assertEquals("pubspec.yaml", pubspec!!.name)
    }

    fun testFindPubspecFileNotFound() {
        val pubspec = JasprProjectService.getInstance(project).findPubspecFile()
        assertNull(pubspec)
    }

    fun testCacheInvalidationOnResetCache() {
        setupNonJasprProject()
        val service = JasprProjectService.getInstance(project)

        // First call caches false
        assertFalse(service.isJasprProject())

        // Now change to jaspr project
        setupJasprProject()

        // After resetCache (called by setupJasprProject), re-detection should pick up jaspr
        assertTrue(service.isJasprProject())
    }
}
