package com.wakeforge.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.models.PremiumStatus
import com.wakeforge.app.domain.models.PurchaseType
import com.wakeforge.app.domain.repositories.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(name = "wakeforge_premium")

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PremiumRepository {

    companion object {
        private const val TAG = "PremiumRepositoryImpl"
        private const val KEY_PREMIUM_STATUS = "premium_status_json"
        private const val KEY_REWARDED_ACCESS = "rewarded_access_map"
        private const val REWARDED_ACCESS_DURATION_MS = 24L * 60L * 60L * 1000L
    }

    private val dataStore: DataStore<Preferences> = context.premiumDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val rewardedAccessMap = ConcurrentHashMap<String, Long>()

    init {
        loadRewardedAccess()
    }

    override fun getPremiumStatus(): Flow<PremiumStatus> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                val jsonStr = prefs[stringPreferencesKey(KEY_PREMIUM_STATUS)]
                if (jsonStr.isNullOrBlank()) {
                    PremiumStatus.Free
                } else {
                    try {
                        parsePremiumStatus(jsonStr)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing premium status", e)
                        PremiumStatus.Free
                    }
                }
            }
    }

    override suspend fun isFeatureUnlocked(feature: PremiumFeature): Boolean {
        val status = getPremiumStatus().first()
        val isPremiumActive = when (status) {
            is PremiumStatus.Free -> false
            is PremiumStatus.Premium -> status.isActive()
            is PremiumStatus.GracePeriod -> status.isActive()
        }
        return isPremiumActive || isRewardedAccessActive(feature)
    }

    override suspend fun grantRewardedAccess(feature: PremiumFeature) {
        val expiryTimestamp = System.currentTimeMillis() + REWARDED_ACCESS_DURATION_MS
        rewardedAccessMap[feature.name] = expiryTimestamp
        saveRewardedAccess()
    }

    override suspend fun isRewardedAccessActive(feature: PremiumFeature): Boolean {
        val key = feature.name
        cleanExpiredEntries()
        val expiry = rewardedAccessMap[key] ?: return false
        if (System.currentTimeMillis() > expiry) {
            rewardedAccessMap.remove(key)
            saveRewardedAccess()
            return false
        }
        return true
    }

    override suspend fun activatePremium(purchaseType: PurchaseType, expiryDate: Long?) {
        val json = JSONObject()
        json.put("type", "PREMIUM")
        json.put("purchaseType", purchaseType.name)
        if (expiryDate != null) {
            json.put("expiryDate", expiryDate)
        } else {
            json.put("expiryDate", JSONObject.NULL)
        }
        savePremiumStatus(json.toString())
    }

    override suspend fun deactivatePremium() {
        savePremiumStatus("")
    }

    override fun getLockedFeatures(): Flow<List<PremiumFeature>> {
        return getPremiumStatus().map { status ->
            val isPremiumActive = when (status) {
                is PremiumStatus.Free -> false
                is PremiumStatus.Premium -> status.isActive()
                is PremiumStatus.GracePeriod -> status.isActive()
            }
            if (isPremiumActive) {
                emptyList()
            } else {
                PremiumFeature.entries.filter { it.isPremium }
            }
        }
    }

    private fun parsePremiumStatus(jsonStr: String): PremiumStatus {
        val json = JSONObject(jsonStr)
        val type = json.optString("type", "")

        return when (type) {
            "PREMIUM" -> {
                val purchaseTypeName = json.optString("purchaseType", "MONTHLY")
                val purchaseType = try {
                    PurchaseType.valueOf(purchaseTypeName)
                } catch (e: IllegalArgumentException) {
                    PurchaseType.MONTHLY
                }
                val expiryDate = if (json.isNull("expiryDate")) null else json.optLong("expiryDate")
                PremiumStatus.Premium(
                    purchaseType = purchaseType,
                    expiryDate = expiryDate
                )
            }
            "GRACE_PERIOD" -> {
                val expiryDate = json.optLong("expiryDate")
                PremiumStatus.GracePeriod(expiryDate = expiryDate)
            }
            else -> PremiumStatus.Free
        }
    }

    private suspend fun savePremiumStatus(json: String) {
        dataStore.edit { prefs ->
            if (json.isBlank()) {
                prefs.remove(stringPreferencesKey(KEY_PREMIUM_STATUS))
            } else {
                prefs[stringPreferencesKey(KEY_PREMIUM_STATUS)] = json
            }
        }
    }

    private fun loadRewardedAccess() {
        scope.launch {
            try {
                val prefs = dataStore.data.first()
                val mapStr = prefs[stringPreferencesKey(KEY_REWARDED_ACCESS)]
                if (!mapStr.isNullOrBlank()) {
                    val json = JSONObject(mapStr)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val expiry = json.getLong(key)
                        if (System.currentTimeMillis() <= expiry) {
                            rewardedAccessMap[key] = expiry
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading rewarded access", e)
            }
        }
    }

    private suspend fun saveRewardedAccess() {
        val json = JSONObject()
        for ((key, value) in rewardedAccessMap) {
            json.put(key, value)
        }
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_REWARDED_ACCESS)] = json.toString()
        }
    }

    private fun cleanExpiredEntries() {
        val now = System.currentTimeMillis()
        val expiredKeys = rewardedAccessMap.entries.filter { it.value <= now }.map { it.key }
        expiredKeys.forEach { rewardedAccessMap.remove(it) }
    }
}
