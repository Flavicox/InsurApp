package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FieldsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FieldsViewModel::class.java)) {
            return FieldsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
