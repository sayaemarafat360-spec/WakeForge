import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.rememberDismissState
package com.wakeforge.app

import android.app.Application
import com.wakeforge.app.BuildConfig
import com.wakeforge.app.core.utils.NotificationUtils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.wakeforge.app.data.ad.AdManager
import com.wakeforge.app.data.premium.PremiumManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Root [Application] subclass for WakeForge.
 *
 * Annotated with [HiltAndroidApp] so Hilt can generate the base
 * component and perform dependency injection throughout the app.
 */
@HiltAndroidApp
class WakeForgeApp : Application() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var premiumManager: PremiumManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // â”€â”€ Logging â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        Timber.i("WakeForge application starting")

        // â”€â”€ Notification Channels â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        NotificationUtils.createAllChannels(applicationContext)

        // â”€â”€ AdMob Initialization â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        adManager.initialize()
        Timber.d("AdMob initialization requested")

        // â”€â”€ Sync premium status to AdManager â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        appScope.launch {
            premiumManager.isPremium().collect { isPremium ->
                adManager.setPremiumStatus(isPremium)
            }
        }
    }

    /**
     * Production-ready [Timber.Tree] that only logs warnings and above to prevent
     * leaking sensitive information in release builds.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= android.util.Log.WARN
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (t != null) {
                android.util.Log.w(tag, message, t)
            } else {
                android.util.Log.w(tag, message)
            }
        }
    }
}

