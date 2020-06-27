package com.khanhlh.substationmonitor.warning

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.util.*

class NotificationIntentService : IntentService("name") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (Objects.requireNonNull(intent.action)) {
                "stopWarning" -> {
                    val stopWarningHandler =
                        Handler(Looper.getMainLooper())
                    stopWarningHandler.post {
                        val warningService = Intent(
                            this@NotificationIntentService,
                            WarningService::class.java
                        )
                        stopService(warningService)
                    }
                }
            }
        }
    }
}