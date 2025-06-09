package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson

sealed class PaymentState {
    object Loading : PaymentState()
    data class Ready(
        val clientSecret: String,
        val customerConfig: PaymentSheet.CustomerConfiguration
    ) : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
    object Idle : PaymentState()
}

class StripePaymentViewModel(private val context: Context) : ViewModel() {

    private val userPrefs = UserPreferences(context)
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val backendUrl = "https://insurapp-api.onrender.com/api/payment/reserve/1"

    fun fetchPaymentSheetData(reserveId: Int, amount: Int) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            try {
                val token = userPrefs.getToken()
                val fullName = userPrefs.getUserName() ?: "Usuario"
                val phone = userPrefs.getUserPhone() ?: "000000000"
                val email = userPrefs.getUserEmail() ?: "email@default.com"

                val jsonBody = JSONObject().apply {
                    put("amount", amount * 100)
                    put("clientName", fullName)
                    put("productName", "Reserva Campo #$reserveId")
                    put("phone", phone)
                    put("email", email)
                }

                val (_, _, result) = "$backendUrl/reserve/$reserveId"
                    .httpPost()
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json")
                    .jsonBody(jsonBody.toString())
                    .responseJson()

                result.fold(
                    success = { json ->
                        val obj = json.obj()
                        val clientSecret = obj.getString("paymentIntent")
                        val ephemeralKey = obj.getString("ephemeralKey")
                        val customerId = obj.getString("customer")
                        val publishableKey = obj.getString("publishableKey")

                        PaymentConfiguration.init(context, publishableKey)

                        val customerConfig = PaymentSheet.CustomerConfiguration(
                            id = customerId,
                            ephemeralKeySecret = ephemeralKey
                        )

                        _paymentState.value = PaymentState.Ready(clientSecret, customerConfig)
                    },
                    failure = { error ->
                        _paymentState.value = PaymentState.Error("HTTP Exception ${'$'}{error.response.statusCode} ${'$'}{error.message}")
                    }
                )
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error("Error: ${'$'}{e.message}")
            }
        }
    }

    fun onPaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> _paymentState.value = PaymentState.Success
            is PaymentSheetResult.Canceled -> _paymentState.value = PaymentState.Idle
            is PaymentSheetResult.Failed -> _paymentState.value = PaymentState.Error(result.error.message ?: "Error desconocido")
        }
    }
}

class StripePaymentViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StripePaymentViewModel(context) as T
    }
}