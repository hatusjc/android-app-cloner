package com.the412banner.bfe.clone

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import com.the412banner.bfe.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/** An installation is counted only after the system package manager confirms it. */
data class CloneEntry(
    val sourcePackage: String,
    val sourceLabel: String,
    val clonePackage: String,
    val cloneLabel: String,
    val launcherClass: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = PENDING,
) {
    companion object {
        const val PENDING = "pending"
        const val INSTALLED = "installed"
    }
}

data class CloneSource(val label: String, val packageName: String, val launcherClass: String)

/**
 * The catalog is local to the host. Removing host app data loses this catalog,
 * NOT the separate Android packages. Never claim otherwise.
 *
 * A single synchronous SharedPreferences commit persists each installation transition.
 * Records are reconciled against PackageManager on resume before they count as installed.
 */
object CloneCatalog {
    private const val PREFS = "clone_hub_catalog_v1"
    private const val KEY = "entries"
    private const val PENDING_TIMEOUT_MS = 30L * 60L * 1000L

    private fun prefs(ctx: Context) = ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun read(ctx: Context): List<CloneEntry> {
        val source = prefs(ctx).getString(KEY, "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(source)
            (0 until arr.length()).mapNotNull { n ->
                val o = arr.optJSONObject(n) ?: return@mapNotNull null
                val src = o.optString("sourcePackage")
                val pkg = o.optString("clonePackage")
                val launcher = o.optString("launcherClass")
                if (src.isBlank() || pkg.isBlank() || launcher.isBlank()) return@mapNotNull null
                CloneEntry(
                    sourcePackage = src,
                    sourceLabel = o.optString("sourceLabel", src),
                    clonePackage = pkg,
                    cloneLabel = o.optString("cloneLabel", pkg),
                    launcherClass = launcher,
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    status = o.optString("status", CloneEntry.PENDING),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun save(ctx: Context, entries: List<CloneEntry>) {
        val arr = JSONArray()
        entries.distinctBy { it.clonePackage }.forEach { e ->
            arr.put(
                JSONObject()
                    .put("sourcePackage", e.sourcePackage)
                    .put("sourceLabel", e.sourceLabel)
                    .put("clonePackage", e.clonePackage)
                    .put("cloneLabel", e.cloneLabel)
                    .put("launcherClass", e.launcherClass)
                    .put("createdAt", e.createdAt)
                    .put("status", e.status),
            )
        }
        check(prefs(ctx).edit().putString(KEY, arr.toString()).commit()) { "Failed to persist clone catalog" }
    }

    fun isInstalled(ctx: Context, pkg: String): Boolean = runCatching {
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(pkg, 0)
        true
    }.getOrDefault(false)

    @Synchronized
    fun reconcile(ctx: Context): List<CloneEntry> {
        val old = read(ctx)
        val now = System.currentTimeMillis()
        val updated = old.mapNotNull { e ->
            when {
                isInstalled(ctx, e.clonePackage) -> e.copy(status = CloneEntry.INSTALLED)
                e.status == CloneEntry.PENDING && now - e.createdAt < PENDING_TIMEOUT_MS -> e
                else -> null
            }
        }
        if (old != updated) save(ctx, updated)
        return updated
    }

    @Synchronized
    fun installed(ctx: Context): List<CloneEntry> =
        reconcile(ctx).filter { it.status == CloneEntry.INSTALLED }

    @Synchronized
    fun occupiedSlots(ctx: Context): Int = reconcile(ctx).size

    @Synchronized
    fun stage(ctx: Context, item: CloneEntry) {
        val all = reconcile(ctx)
        require(all.none { it.clonePackage == item.clonePackage }) { "Clone package already tracked" }
        save(ctx, all + item.copy(status = CloneEntry.PENDING))
    }

    /** Called when the Android installer activity returns. A cancellation never consumes a slot. */
    @Synchronized
    fun confirmInstallerReturn(ctx: Context, pkg: String): Boolean {
        val installed = isInstalled(ctx, pkg)
        val all = read(ctx).filterNot { it.clonePackage == pkg }
        val existing = read(ctx).firstOrNull { it.clonePackage == pkg }
        if (installed && existing != null) save(ctx, all + existing.copy(status = CloneEntry.INSTALLED))
        else save(ctx, all)
        return installed
    }

    @Synchronized
    fun dropIfAbsent(ctx: Context, pkg: String): Boolean {
        if (isInstalled(ctx, pkg)) return false
        val old = read(ctx)
        val next = old.filterNot { it.clonePackage == pkg }
        if (old.size != next.size) save(ctx, next)
        return old.size != next.size
    }

    fun nextClonePackage(ctx: Context, source: String): String {
        val tracked = reconcile(ctx).map { it.clonePackage }.toSet()
        return (1..9999).asSequence().map { "$source.clone$it" }
            .firstOrNull { it !in tracked && !isInstalled(ctx, it) }
            ?: error("No available clone package suffix")
    }

    fun userApplications(ctx: Context): List<CloneSource> {
        val pm = ctx.packageManager
        val knownClones = reconcile(ctx).map { it.clonePackage }.toSet()
        return runCatching {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
                .asSequence()
                .filter { it.packageName != ctx.packageName && it.packageName !in knownClones }
                .filterNot {
                    CloneRules.isSystemApp(it.flags, ApplicationInfo.FLAG_SYSTEM, ApplicationInfo.FLAG_UPDATED_SYSTEM)
                }
                .mapNotNull { app ->
                    val component = pm.getLaunchIntentForPackage(app.packageName)?.component ?: return@mapNotNull null
                    CloneSource(
                        label = runCatching { app.loadLabel(pm).toString() }.getOrDefault(app.packageName),
                        packageName = app.packageName,
                        launcherClass = component.className,
                    )
                }
                .sortedBy { it.label.lowercase(Locale.getDefault()) }
                .toList()
        }.getOrDefault(emptyList())
    }

    fun launch(ctx: Context, entry: CloneEntry): Boolean {
        if (reconcile(ctx).none { it.clonePackage == entry.clonePackage && it.status == CloneEntry.INSTALLED }) return false
        return runCatching {
            val intent = Intent.makeMainActivity(ComponentName(entry.clonePackage, entry.launcherClass))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            ctx.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun showAndroidAppInfo(ctx: Context, pkg: String) {
        ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$pkg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

/** Commercial Pro needs verified billing later; debug test builds expose an explicit QA switch only. */
object ClonePlan {
    private const val PREFS = "clone_hub_plan"
    fun isPro(ctx: Context): Boolean =
        BuildConfig.DEBUG && ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("qa_pro", false)

    fun setQaPro(ctx: Context, enabled: Boolean) {
        if (BuildConfig.DEBUG) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("qa_pro", enabled).apply()
        }
    }
}
