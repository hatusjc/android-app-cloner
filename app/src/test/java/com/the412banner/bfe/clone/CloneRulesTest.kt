package com.the412banner.bfe.clone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloneRulesTest {
    @Test
    fun freePlanAllowsAtMostTwoOccupiedSlots() {
        assertTrue(CloneRules.canCreate(0, isPro = false))
        assertTrue(CloneRules.canCreate(1, isPro = false))
        assertFalse(CloneRules.canCreate(2, isPro = false))
        assertFalse(CloneRules.canCreate(3, isPro = false))
        assertEquals(2, CloneRules.remainingFreeSlots(0))
        assertEquals(1, CloneRules.remainingFreeSlots(1))
        assertEquals(0, CloneRules.remainingFreeSlots(2))
    }

    @Test
    fun proHasNoAppEnforcedCloneLimit() {
        assertTrue(CloneRules.canCreate(2, isPro = true))
        assertTrue(CloneRules.canCreate(500, isPro = true))
    }

    @Test
    fun downloadedAppFilterExcludesSystemAndUpdatedSystemApps() {
        assertFalse(CloneRules.isSystemApp(0, 1, 128))
        assertTrue(CloneRules.isSystemApp(1, 1, 128))
        assertTrue(CloneRules.isSystemApp(128, 1, 128))
        assertTrue(CloneRules.isSystemApp(129, 1, 128))
    }

    @Test
    fun hidingLauncherDisablesBothPhoneAndTvLaunchers() {
        assertEquals(
            "com.hatusjc.androidappcloner.NO_LAUNCHER",
            CloneRules.launcherCategoryForClone("android.intent.category.LAUNCHER"),
        )
        assertEquals(
            "com.hatusjc.androidappcloner.NO_LEANBACK_LAUNCHER",
            CloneRules.launcherCategoryForClone("android.intent.category.LEANBACK_LAUNCHER"),
        )
        assertEquals(
            "android.intent.category.DEFAULT",
            CloneRules.launcherCategoryForClone("android.intent.category.DEFAULT"),
        )
    }
}
