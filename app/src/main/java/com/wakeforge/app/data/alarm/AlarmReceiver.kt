package com.wakeforge.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wakeforge.app.presentation.MainActivity
import com.wakeforge.app.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        const val EXTRA_ALARM_ID = "com.wakeforge.app.EXTRA_ALARM_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ALARM_ACTION_RING) {
            Log.w(TAG, "Received unexpected action: ${intent.action}")
            return
        }

        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
        if (alarmId.isNullOrBlank()) {
            Log.e(TAG, "Alarm ID is null or blank in received intent")
            return
        }

        Log.d(TAG, "Alarm received for alarm ID: $alarmId")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            action = AlarmScheduler.ALARM_ACTION_RING
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
        }

        context.startActivity(activityIntent)
    }
}
