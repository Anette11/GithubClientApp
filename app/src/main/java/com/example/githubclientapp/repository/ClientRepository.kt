package com.example.githubclientapp.repository

import androidx.lifecycle.LiveData
import com.example.githubclientapp.api.RetrofitInstance
import com.example.githubclientapp.database.ClientDao
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded

class ClientRepository(
    private val clientDao: ClientDao
) {
    suspend fun getUserAllRepositories(userName: String, page: Int) =
        RetrofitInstance.clientWebservice.getUserAllRepositories(userName, page)

    suspend fun saveUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded) =
        clientDao.saveUserSingleRepository(userSingleRepositoryDownloaded)

    fun getAllSavedUserSingleRepositories(): LiveData<List<UserSingleRepositoryDownloaded>> =
        clientDao.getAllSavedUserSingleRepositories()

    suspend fun deleteUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded) =
        clientDao.deleteUserSingleRepository(userSingleRepositoryDownloaded)
}