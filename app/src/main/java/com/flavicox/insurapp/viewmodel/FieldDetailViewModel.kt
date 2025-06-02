package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class FieldDetailViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)

    private val _fieldDetail = MutableStateFlow<Field?>(null)
    val fieldDetail: StateFlow<Field?> = _fieldDetail

    /**
     * Llama a GET /api/fields/{fieldId} para obtener tipoField, numberField y price.
     */
    fun loadField(fieldId: Int) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken()
                if (!token.isNullOrEmpty()) {
                    val bearer = "Bearer $token"
                    val response: Field =
                        RetrofitInstance.authApi.getFieldById(fieldId, bearer)
                    _fieldDetail.value = response
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _fieldDetail.value = null
            }
        }
    }
}
