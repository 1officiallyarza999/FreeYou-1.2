package com.freeyou

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.freeyou.data.BlockRepo

class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        BlockRepo.init(context)
        return if (BlockRepo.state.value.strict) {
            "מצב קפדני פעיל. פתח את FreeYou והשתמש במנגנון שחרור."
        } else {
            "ההגנה תבוטל."
        }
    }
}
