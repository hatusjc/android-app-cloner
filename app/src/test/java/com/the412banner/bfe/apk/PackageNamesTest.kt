package com.the412banner.bfe.apk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * JVM-only checks for package IDs before rewriting and signing a clone.
 * Tests reject malformed names before PackageManager installation.
 */
class PackageNamesTest {
    @Test
    fun validIdentifiersAreAccepted() {
        assertNull(PackageNames.problem("com.example.app"))
        assertNull(PackageNames.problem("org.example_1.clone2"))
        assertNull(PackageNames.problem("a.b"))
    }

    @Test
    fun emptyAndSingleSegmentAreRejected() {
        assertNotNull(PackageNames.problem(""))
        assertNotNull(PackageNames.problem("   "))
        assertNotNull(PackageNames.problem("one"))
    }

    @Test
    fun malformedDotSegmentsAreRejected() {
        assertNotNull(PackageNames.problem(".example"))
        assertNotNull(PackageNames.problem("com..example"))
        assertNotNull(PackageNames.problem("com.example."))
    }

    @Test
    fun invalidIdentifierCharactersAreRejected() {
        assertNotNull(PackageNames.problem("com.12example"))
        assertNotNull(PackageNames.problem("com.example-dash"))
        assertNotNull(PackageNames.problem("com.example space"))
        assertNotNull(PackageNames.problem("com.exämple"))
    }

    @Test
    fun stageTransitionsHaveNonterminalRunningStates() {
        assertEquals(true, ApkJobStage.PARSING.isRunning)
        assertEquals(true, ApkJobStage.SIGNING.isRunning)
        assertEquals(false, ApkJobStage.DONE.isRunning)
        assertEquals(false, ApkJobStage.ERROR.isRunning)
        assertEquals(false, ApkJobStage.CANCELLED.isRunning)
    }
}
