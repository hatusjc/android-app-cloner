package com.the412banner.bfe.clone

/**
 * Product limits independent from Android/package-manager APIs so they can be unit tested.
 * "Active" includes installs currently waiting for the system installer confirmation.
 */
object CloneRules {
    const val FREE_LIMIT = 2

    fun canCreate(activeCount: Int, isPro: Boolean): Boolean =
        isPro || activeCount < FREE_LIMIT

    fun remainingFreeSlots(activeCount: Int): Int =
        (FREE_LIMIT - activeCount).coerceAtLeast(0)

    fun isSystemApp(flags: Int, systemFlag: Int, updatedSystemFlag: Int): Boolean =
        flags and (systemFlag or updatedSystemFlag) != 0

    fun launcherCategoryForClone(name: String): String =
        when (name) {
            "android.intent.category.LAUNCHER" -> "com.hatusjc.androidappcloner.NO_LAUNCHER"
            "android.intent.category.LEANBACK_LAUNCHER" -> "com.hatusjc.androidappcloner.NO_LEANBACK_LAUNCHER"
            else -> name
        }
}
