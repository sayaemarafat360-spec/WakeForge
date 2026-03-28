package com.wakeforge.app.data.ad

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {

    companion object {
        private const val TAG = "AdManager"

        const val BANNER_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/5224354917"
    }

    val bannerAdUnitId: String = BANNER_AD_UNIT_ID_TEST
    val interstitialAdUnitId: String = INTERSTITIAL_AD_UNIT_ID_TEST
    val rewardedAdUnitId: String = REWARDED_AD_UNIT_ID_TEST

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading: Boolean = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading: Boolean = false

    private var isInitialized: Boolean = false

    @Volatile
    private var isUserPremium: Boolean = false

    /** Call this from PremiumManager whenever premium status changes */
    fun setPremiumStatus(isPremium: Boolean) {
        isUserPremium = isPremium
    }

    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "AdMob already initialized")
            return
        }

        MobileAds.initialize { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    TAG,
                    "Adapter: $adapterClass, Status: ${status?.initializationState?.name}, " +
                        "Latency: ${status?.latencyMillis}ms"
                )
            }
            isInitialized = true
            Log.d(TAG, "AdMob initialized successfully")
        }
    }

    fun shouldShowAds(): Boolean {
        return !isInitialized || !isUserPremium
    }

    fun loadInterstitialAd() {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping interstitial ad load - user is premium")
            return
        }

        if (isInterstitialLoading) {
            Log.d(TAG, "Interstitial ad already loading")
            return
        }

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(interstitialAdUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
                isInterstitialLoading = false
                Log.e(TAG, "Interstitial ad failed to load: ${adError.message}")
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isInterstitialLoading = false
                Log.d(TAG, "Interstitial ad loaded successfully")

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        Log.d(TAG, "Interstitial ad dismissed")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad showed")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Interstitial ad clicked")
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Interstitial ad impression recorded")
                    }
                }
            }
        })
    }

    fun showInterstitialAd(
        activity: Activity,
        onDismissed: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (!shouldShowAds()) {
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed()
                    Log.d(TAG, "Interstitial ad dismissed after show")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onFailed()
                    Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed to user")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Interstitial ad clicked by user")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Interstitial ad impression recorded")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "No interstitial ad available, loading one for next time")
            loadInterstitialAd()
            onDismissed()
        }
    }

    fun loadRewardedAd(
        activity: Activity,
        onEarned: () -> Unit,
        onFailed: () -> Unit
    ) {
        if (isRewardedLoading) {
            Log.d(TAG, "Rewarded ad already loading")
            return
        }

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, rewardedAdUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isRewardedLoading = false
                onFailed()
                Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isRewardedLoading = false
                Log.d(TAG, "Rewarded ad loaded successfully")

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        Log.d(TAG, "Rewarded ad dismissed")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        rewardedAd = null
                        onFailed()
                        Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad showed")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Rewarded ad clicked")
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Rewarded ad impression recorded")
                    }
                }

                ad.show(activity) { rewardItem: RewardItem ->
                    Log.d(
                        TAG,
                        "User earned reward: type=${rewardItem.type}, amount=${rewardItem.amount}"
                    )
                    onEarned()
                }
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onEarned: () -> Unit,
        onFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    Log.d(TAG, "Rewarded ad dismissed after show")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    onFailed()
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed to user")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Rewarded ad clicked by user")
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Rewarded ad impression recorded")
                }
            }
            ad.show(activity) { rewardItem: RewardItem ->
                Log.d(
                    TAG,
                    "User earned reward: type=${rewardItem.type}, amount=${rewardItem.amount}"
                )
                onEarned()
            }
        } else {
            Log.d(TAG, "No rewarded ad available to show")
            onFailed()
        }
    }

    fun isInterstitialReady(): Boolean {
        return interstitialAd != null
    }

    fun isRewardedReady(): Boolean {
        return rewardedAd != null
    }

    fun destroy() {
        interstitialAd = null
        rewardedAd = null
        isInterstitialLoading = false
        isRewardedLoading = false
    }
}
