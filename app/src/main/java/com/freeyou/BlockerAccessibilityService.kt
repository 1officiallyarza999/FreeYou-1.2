package com.freeyou

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.freeyou.data.AdultBlockEngine
import com.freeyou.data.BlockRepo

class BlockerAccessibilityService : AccessibilityService() {

    private var lastHit: String? = null
    private var lastHitAt = 0L

    companion object {
        private const val TAG = "FreeYouBlocker"

        // Comprehensive list of browser URL/omnibox resource IDs across all Android browsers
        private val BROWSER_URL_IDS = listOf(
            "com.android.chrome:id/url_bar",
            "com.android.chrome:id/search_box_text",
            "com.android.chrome:id/line_1",
            "com.android.chrome:id/toolbar",
            "org.mozilla.firefox:id/url_bar_title",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox:id/url_bar",
            "com.brave.browser:id/url_bar",
            "com.brave.browser:id/search_box_text",
            "com.opera.browser:id/url_field",
            "com.opera.mini.native:id/url_field",
            "com.opera.gx:id/url_field",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.sec.android.app.sbrowser:id/url_bar",
            "com.microsoft.emmx:id/url_bar",
            "com.microsoft.emmx:id/search_box",
            "com.duckduckgo.mobile.android:id/omnibarTextInput",
            "com.google.android.googlequicksearchbox:id/googleapp_search_box",
            "com.kiwibrowser.browser:id/url_bar",
            "com.vivaldi.browser:id/url_bar"
        )

        // Social apps where adult content/creator leaks frequently appear
        private val SOCIAL_APPS = setOf(
            "com.twitter.android",
            "com.x.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.instagram.android",
            "com.reddit.frontpage",
            "org.telegram.messenger",
            "org.telegram.plus"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "FreeYou Accessibility Blocker connected.")
        BlockRepo.init(this)
        MentorOverlay.init(this)
    }

    override fun onAccessibilityEvent(ev: AccessibilityEvent?) {
        if (ev == null) return
        BlockRepo.init(this)

        val pkg = ev.packageName?.toString() ?: return
        if (pkg == packageName) return // Ignore FreeYou itself

        // 1. Check if the entire app is in the user's blocked apps list
        val appBlock = BlockRepo.isAppBlocked(pkg, ev.className?.toString())
        if (appBlock != null) {
            trigger(appBlock)
            return
        }

        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() == packageName) {
            @Suppress("DEPRECATION")
            root.recycle()
            return
        }
        
        try {
            // Anti-bypass for Accessibility Settings
            if (pkg == "com.android.settings" && BlockRepo.state.value.strict) {
                // If the user is trying to disable the service in settings
                val settingsBlocked = checkSettingsAntiBypass(root)
                if (settingsBlocked) {
                    trigger("הגדרות נגישות (מצב קשוח פעיל)")
                    return
                }
            }

            // 2. Scan window for blocked URLs, adult domains, and prohibited keywords
            val detectedBlock = inspectNodeHierarchy(root, pkg, 0)

            if (detectedBlock != null) {
                trigger(detectedBlock)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting accessibility window: ${e.message}")
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    /**
     * Recursively inspects the accessibility node tree for:
     * 1. Browser URL bars / omniboxes
     * 2. Visible URLs or domains (e.g. pornhub.com, onlyfans.com)
     * 3. Prohibited adult terms and search queries (OnlyFans, porn, Hebrew keywords)
     */
    private fun inspectNodeHierarchy(node: AccessibilityNodeInfo, pkg: String, depth: Int): String? {
        if (depth > 9) return null
        if (node.packageName?.toString() == packageName) return null

        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        // A. Direct check on known browser URL view IDs
        if (viewId.isNotEmpty()) {
            val isKnownUrlId = BROWSER_URL_IDS.any { viewId.contains(it) } ||
                    viewId.contains("url") ||
                    viewId.contains("omnibox") ||
                    viewId.contains("location_bar") ||
                    viewId.contains("search_box")

            if (isKnownUrlId && !text.isNullOrBlank()) {
                val blocked = BlockRepo.isUrlBlocked(text)
                if (blocked != null) return blocked

                val contentBlocked = BlockRepo.checkContentTrigger(text)
                if (contentBlocked != null) return contentBlocked
            }
        }

        // B. Check text content for URLs and adult keywords
        if (!text.isNullOrBlank()) {
            // Check if text looks like a URL or domain
            if (text.contains(".") || text.startsWith("http://") || text.startsWith("https://")) {
                val blocked = BlockRepo.isUrlBlocked(text)
                if (blocked != null) return blocked
            }

            // Check if text contains adult triggers (e.g. OnlyFans, Pornhub, 18+ terms)
            val contentBlocked = BlockRepo.checkContentTrigger(text)
            if (contentBlocked != null) {
                return contentBlocked
            }
        }

        // C. Check content description
        if (!desc.isNullOrBlank()) {
            if (desc.contains(".") || desc.startsWith("http")) {
                val blocked = BlockRepo.isUrlBlocked(desc)
                if (blocked != null) return blocked
            }
            val contentBlocked = BlockRepo.checkContentTrigger(desc)
            if (contentBlocked != null) {
                return contentBlocked
            }
        }

        // D. Recurse through children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val res = inspectNodeHierarchy(child, pkg, depth + 1)
            @Suppress("DEPRECATION")
            child.recycle()
            if (res != null) return res
        }

        return null
    }

    private fun checkSettingsAntiBypass(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        
        // If the screen contains our app name or package and is in settings
        if (text.contains("freeyou") || desc.contains("freeyou")) {
            return true
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val res = checkSettingsAntiBypass(child)
            @Suppress("DEPRECATION")
            child.recycle()
            if (res) return true
        }
        return false
    }

    private fun trigger(target: String) {
        val now = System.currentTimeMillis()
        // Prevent rapid duplicate hits within 1.5 seconds for the same target
        if (target == lastHit && now - lastHitAt < 1500) return
        lastHit = target
        lastHitAt = now

        Log.w(TAG, "INTERCEPT TRIGGERED: $target")

        // 1. Immediately press system BACK to exit the adult page / app
        performGlobalAction(GLOBAL_ACTION_BACK)

        val count = BlockRepo.bumpAttempt()
        val nextRoute = if (count <= 1) "journal" else "mission"

        BlockRepo.setPendingRoute(nextRoute, target, count)

        // 2. Display the Glass Floating Overlay if overlay permission is granted
        if (Settings.canDrawOverlays(this)) {
            MentorOverlay.show(
                this,
                count,
                onContinue = { launchApp(nextRoute, target, count) },
                onBack = { performGlobalAction(GLOBAL_ACTION_HOME) }
            )
        }

        // 3. Guarantee interception by launching FreeYou Intercept Screen
        launchApp("intercept", target, count)
    }

    private fun launchApp(route: String, target: String, count: Int) {
        val i = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra("freeyou_route", route)
            putExtra("freeyou_target", target)
            putExtra("freeyou_count", count)
        }
        startActivity(i)
    }

    override fun onInterrupt() {
        Log.i(TAG, "FreeYou Accessibility Blocker interrupted.")
    }
}
