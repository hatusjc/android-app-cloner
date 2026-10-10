package com.the412banner.bfe.clone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import com.the412banner.bfe.R

/**
 * Pinned shortcuts belong to the Clone Hub package, not the separate clone APK.
 * Android removes host-owned pinned shortcuts when the host is uninstalled; the
 * cloned Android packages themselves are NOT cascaded by that uninstall.
 */
object CloneShortcuts {
    const val EXTRA_PACKAGE = "com.hatusjc.androidappcloner.EXTRA_CLONE_PACKAGE"

    fun pin(ctx: Context, entry: CloneEntry): Boolean {
        if (!ClonePlan.isPro(ctx) || !CloneCatalog.isInstalled(ctx, entry.clonePackage)) return false
        val manager = ctx.getSystemService(ShortcutManager::class.java) ?: return false
        if (!manager.isRequestPinShortcutSupported) return false
        val intent = Intent(ctx, CloneShortcutActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(EXTRA_PACKAGE, entry.clonePackage)
        val shortcut = ShortcutInfo.Builder(ctx, "clonehub-${entry.clonePackage}")
            .setShortLabel(entry.cloneLabel.take(24))
            .setLongLabel(entry.cloneLabel.take(48))
            .setIcon(Icon.createWithResource(ctx, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()
        return runCatching { manager.requestPinShortcut(shortcut, null) }.getOrDefault(false)
    }
}

/** Explicit exported entry point for launchers; destination is always allowlisted locally. */
class CloneShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requested = intent?.getStringExtra(CloneShortcuts.EXTRA_PACKAGE)
        val valid = CloneCatalog.installed(this).firstOrNull { it.clonePackage == requested }
        val launched = valid != null && ClonePlan.isPro(this) && CloneCatalog.launch(this, valid)
        if (!launched) {
            Toast.makeText(this, "Atalho indisponível. Abra o Clone Hub.", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
