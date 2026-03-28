package com.wakeforge.app

import android.app.Application
import com.wakeforge.app.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.wakeforge.app.data.ad.AdManager
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

    override fun onCreate() {
        super.onCreate()

        // ── Logging ────────────────────────────────────────────────────────────
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        Timber.i("WakeForge application starting")

        // ── AdMob Initialization ───────────────────────────────────────────────
        adManager.initialize()
        Timber.d("AdMob initialization requested")
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
