package com.wakeforge.app.presentation.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeforge.app.data.premium.PremiumManager
import com.wakeforge.app.domain.models.PremiumFeature
import com.wakeforge.app.domain.models.PurchaseType
import com.wakeforge.app.domain.usecases.premium.GetPremiumFeaturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val getPremiumFeaturesUseCase: GetPremiumFeaturesUseCase,
    private val premiumManager: PremiumManager
) : ViewModel() {

    data class PremiumUiState(
        val isPremium: Boolean = false,
        val features: List<FeatureStatus> = emptyList(),
        val showPurchaseDialog: Boolean = false,
        val selectedPlan: PricingPlan = PricingPlan.MONTHLY,
        val purchaseError: String? = null,
        val isRestoring: Boolean = false
    )

    data class FeatureStatus(
        val feature: PremiumFeature,
        val isUnlocked: Boolean
    )

    enum class PricingPlan(val displayName: String, val price: String, val subtitle: String) {
        MONTHLY(displayName = "Monthly", price = "$4.99", subtitle = "/month"),
        YEARLY(displayName = "Yearly", price = "$39.99", subtitle = "/year — Save 33%"),
        LIFETIME(displayName = "Lifetime", price = "$79.99", subtitle = "One-time purchase")
    }

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                premiumManager.isPremium(),
                getPremiumFeaturesUseCase()
            ) { isPremium, featureStatuses ->
                PremiumUiState(
                    isPremium = isPremium,
                    features = featureStatuses.map { fs ->
                        FeatureStatus(
                            feature = fs.feature,
                            isUnlocked = fs.isUnlocked
                        )
                    }
                )
            }.collect { newState ->
                _uiState.value = _uiState.value.copy(
                    isPremium = newState.isPremium,
                    features = newState.features
                )
            }
        }
    }

    fun selectPlan(plan: PricingPlan) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun purchaseMonthly() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(purchaseError = null)
            try {
                premiumManager.activate(
                    purchaseType = PurchaseType.MONTHLY,
                    expiryDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
                )
                _uiState.value = _uiState.value.copy(showPurchaseDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    purchaseError = e.message ?: "Purchase failed. Please try again."
                )
            }
        }
    }

    fun purchaseYearly() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(purchaseError = null)
            try {
                premiumManager.activate(
                    purchaseType = PurchaseType.YEARLY,
                    expiryDate = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000
                )
                _uiState.value = _uiState.value.copy(showPurchaseDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    purchaseError = e.message ?: "Purchase failed. Please try again."
                )
            }
        }
    }

    fun purchaseLifetime() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(purchaseError = null)
            try {
                premiumManager.activate(
                    purchaseType = PurchaseType.LIFETIME,
                    expiryDate = null
                )
                _uiState.value = _uiState.value.copy(showPurchaseDialog = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    purchaseError = e.message ?: "Purchase failed. Please try again."
                )
            }
        }
    }

    fun purchaseSelectedPlan() {
        when (_uiState.value.selectedPlan) {
            PricingPlan.MONTHLY -> purchaseMonthly()
            PricingPlan.YEARLY -> purchaseYearly()
            PricingPlan.LIFETIME -> purchaseLifetime()
        }
    }

    fun restorePurchase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true)
            try {
                premiumManager.restorePurchases()
                _uiState.value = _uiState.value.copy(isRestoring = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    purchaseError = "Restore failed. No previous purchases found."
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(purchaseError = null)
    }
}
