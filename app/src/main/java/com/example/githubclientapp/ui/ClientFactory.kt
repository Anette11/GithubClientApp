package com.example.githubclientapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.githubclientapp.repository.ClientRepository

class ClientFactory(
    private val application: Application,
    private val clientRepository: ClientRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientViewModel::class.java)) {
            return ClientViewModel(application, clientRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}