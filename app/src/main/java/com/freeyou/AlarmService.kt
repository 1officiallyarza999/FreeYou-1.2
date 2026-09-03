package com.freeyou

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vib: Vibrator? = null
    private val ch = "freeyou_mission"

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val night = intent?.getBooleanExtra("night", false) ?: false
        startForeground(42, buildNotification(night))
        ring()
        return START_STICKY
    }

    private fun buildNotification(night: Boolean): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(ch, "משימת גוף", NotificationManager.IMPORTANCE_HIGH).apply {
                setBypassDnd(true)
                enableVibration(true)
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("freeyou_route", "mission")
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, ch)
            .setContentTitle("הדיבורים נגמרו. עכשיו הגוף.")
            .setContentText(if (night) "פרוטוקול ביתי, 12 דקות" else "צא מהבית. הצלצול נפסק כשתתרחק 250 מטר.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .build()
    }

    private fun ring() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            player = MediaPlayer().apply {
                setDataSource(this@AlarmService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(AudioManager.STREAM_ALARM, (max * 0.45).toInt(), 0)
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
                } catch (_: Exception) {}
            }, 45_000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 420, 200, 420, 900)
            vib?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            player?.run { if (isPlaying) stop(); release() }
            vib?.cancel()
        } catch (_: Exception) {}
        super.onDestroy()
    }
}
