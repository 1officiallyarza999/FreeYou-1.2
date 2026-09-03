package com.freeyou

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.freeyou.data.BlockRepo

class BlockerAccessibilityService : AccessibilityService() {

    private var lastHit: String? = null
    private var lastHitAt = 0L

    private val BROWSER_URL_IDS = listOf(
        "com.android.chrome:id/url_bar",
        "org.mozilla.firefox:id/url_bar_title",
        "com.brave.browser:id/url_bar",
        "com.opera.browser:id/url_field",
        "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "com.microsoft.emmx:id/url_bar",
        "com.duckduckgo.mobile.android:id/omnibarTextInput"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        BlockRepo.init(this)
        MentorOverlay.init(this)
    }

    override fun onAccessibilityEvent(ev: AccessibilityEvent?) {
        if (ev == null) return
        BlockRepo.init(this)

        val pkg = ev.packageName?.toString() ?: return
        if (pkg == packageName) return

        val now = System.currentTimeMillis()
        if (now - lastHitAt < 1200) return

        val appBlock = BlockRepo.isAppBlocked(pkg, ev.className?.toString())
        if (appBlock != null) {
            trigger(appBlock)
            return
        }

        val root = rootInActiveWindow ?: return
        try {
            val url = extractUrl(root)
            if (url != null) {
                val blocked = BlockRepo.isUrlBlocked(url)
                if (blocked != null) {
                    trigger(blocked)
                }
            }
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    private fun extractUrl(root: AccessibilityNodeInfo): String? {
        for (id in BROWSER_URL_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                val text = nodes[0].text?.toString()
                nodes.forEach {
                    @Suppress("DEPRECATION")
                    it.recycle()
                }
                if (!text.isNullOrBlank()) return text
            }
        }
        return scanForUrl(root, 0)
    }

    private fun scanForUrl(node: AccessibilityNodeInfo, depth: Int): String? {
        if (depth > 6) return null
        val t = node.text?.toString()?.trim()
        if (t != null && (t.startsWith("http://") || t.startsWith("https://") ||
                    (t.contains(".") && !t.contains(" ") && t.length in 4..60 &&
                            (t.endsWith(".com") || t.endsWith(".co.il") || t.endsWith(".org") || t.endsWith(".net") || t.endsWith(".io"))))
        ) {
            return t
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val res = scanForUrl(child, depth + 1)
            @Suppress("DEPRECATION")
            child.recycle()
            if (res != null) return res
        }
        return null
    }

    private fun trigger(target: String) {
        val now = System.currentTimeMillis()
        if (target == lastHit && now - lastHitAt < 2500) return
        lastHit = target
        lastHitAt = now

        performGlobalAction(GLOBAL_ACTION_BACK)

        val count = BlockRepo.bumpAttempt()
        val nextRoute = if (count <= 1) "journal" else "mission"

        BlockRepo.setPendingRoute(nextRoute, target, count)

        if (Settings.canDrawOverlays(this)) {
            MentorOverlay.show(
                this,
                count,
                onContinue = { launchApp(nextRoute, target, count) },
                onBack = { performGlobalAction(GLOBAL_ACTION_HOME) }
            )
        } else {
            launchApp("intercept", target, count)
        }
    }

    private fun launchApp(route: String, target: String, count: Int) {
        val i = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("freeyou_route", route)
            putExtra("freeyou_target", target)
            putExtra("freeyou_count", count)
        }
        startActivity(i)
    }

    override fun onInterrupt() {}
}
