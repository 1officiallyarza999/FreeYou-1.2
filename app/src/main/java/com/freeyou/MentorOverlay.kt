package com.freeyou

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.freeyou.data.BlockRepo
import java.util.Locale

object MentorOverlay {

    private var view: View? = null
    private var tts: TextToSpeech? = null
    private var wm: WindowManager? = null

    private val FIRST = listOf(
        "רגע. אני מכיר את הרגע הזה.",
        "עצור שנייה. אתה לא באמת רוצה את זה.",
        "היי. תסתכל עליי לפני שאתה ממשיך.",
        "אני החלק בך שקם מוקדם. תן לי שתי שניות.",
        "לא הגעת לכאן במקרה, וגם אני לא."
    )
    private val SECOND = listOf(
        "זו הפעם השנייה היום. אני עדיין כאן.",
        "שוב. ואני עדיין לא מוותר עליך.",
        "הפעם זה לא ייגמר בכתיבה."
    )
    private val BODY = listOf(
        "מה שאתה מרגיש עכשיו הוא גל. הוא עולה, מגיע לשיא, ודועך תוך רבע שעה. אתה לא צריך לנצח אותו — רק לשרוד אותו.",
        "בעוד עשר דקות זה ייגמר, ואתה תישאר עם מה שבחרת. תבחר את מה שתרצה לזכור מחר בבוקר.",
        "הראש שאתה צריך לעסק ולמשפחה הוא אותו ראש בדיוק. כל פעם שאתה עומד כאן, אתה מחזיר אותו אליך."
    )

    fun init(ctx: Context) {
        if (tts != null) return
        tts = TextToSpeech(ctx.applicationContext) { st ->
            if (st == TextToSpeech.SUCCESS) {
                tts?.language = Locale("he", "IL")
                tts?.setSpeechRate(0.94f)
                tts?.setPitch(0.9f)
            }
        }
    }

    fun isShowing() = view != null

    fun show(ctx: Context, count: Int, onContinue: () -> Unit, onBack: () -> Unit) {
        if (view != null || !Settings.canDrawOverlays(ctx)) return
        init(ctx)

        val d = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        val root = FrameLayout(ctx).apply { setBackgroundColor(Color.parseColor("#F0050507")) }

        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(26), dp(0), dp(26), dp(0))
        }

        val orb = View(ctx).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = dp(78).toFloat()
                colors = intArrayOf(
                    Color.parseColor("#FFB020"),
                    Color.parseColor("#66FB2E6B"),
                    Color.TRANSPARENT
                )
            }
        }
        col.addView(orb, LinearLayout.LayoutParams(dp(156), dp(156)).apply { bottomMargin = dp(10) })

        fun text(s: String, size: Float, color: String, bold: Boolean, top: Int) =
            TextView(ctx).apply {
                this.text = s
                setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                setTextColor(Color.parseColor(color))
                gravity = Gravity.CENTER
                textDirection = View.TEXT_DIRECTION_RTL
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
                setLineSpacing(dp(5).toFloat(), 1f)
                setPadding(0, dp(top), 0, 0)
            }

        val opener = (if (count <= 1) FIRST else SECOND).random()
        val bodyText = BODY.random()
        val reasons = BlockRepo.state.value.reasons
        val reason = reasons.randomOrNull()

        col.addView(text(opener, 24f, "#F4F5F7", true, 0))
        col.addView(text(bodyText, 15.5f, "#8EF4F5F7", false, 14))
        if (reason != null) {
            col.addView(text("\"$reason\"", 16f, "#FFB020", false, 20))
        }

        fun button(label: String, fill: Int, stroke: Int, textColor: String, onTap: () -> Unit) =
            TextView(ctx).apply {
                this.text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(Color.parseColor(textColor))
                gravity = Gravity.CENTER
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(16), 0, dp(16))
                background = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(fill)
                    setStroke(dp(1), stroke)
                }
                setOnClickListener { hide(ctx); onTap() }
            }

        val lp = { top: Int ->
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { topMargin = dp(top) }
        }

        col.addView(
            button("אני חוזר. תודה.", Color.parseColor("#FFB020"), Color.parseColor("#33FFFFFF"), "#1A1005") { onBack() },
            lp(24)
        )

        val nextLabel = if (count <= 1) "יומנו של גבר — כותב עכשיו" else "משימת גוף — יוצא עכשיו"
        col.addView(
            button(nextLabel, Color.TRANSPARENT, Color.parseColor("#29FFFFFF"), "#F4F5F7") { onContinue() },
            lp(10)
        )

        root.addView(
            col,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                .apply { gravity = Gravity.CENTER }
        )

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm?.addView(root, params)
        view = root

        tts?.speak("$opener $bodyText", TextToSpeech.QUEUE_FLUSH, null, "mentor_overlay")
    }

    fun hide(ctx: Context) {
        tts?.stop()
        view?.let {
            try {
                (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(it)
            } catch (_: Exception) {}
        }
        view = null
    }
}
