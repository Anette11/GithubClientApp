package com.example.githubclientapp.util

import android.app.Application
import com.example.githubclientapp.database.ClientDatabase
import com.example.githubclientapp.repository.ClientRepository

class ClientApplication : Application() {
    private val clientDatabase by lazy { ClientDatabase.getClientDatabase(this) }
    val clientRepository by lazy { ClientRepository(clientDatabase.clientDao()) }
}