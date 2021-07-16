package com.example.githubclientapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded)

    @Query("SELECT * FROM user_repositories")
    fun getAllSavedUserSingleRepositories(): LiveData<List<UserSingleRepositoryDownloaded>>

    @Delete
    suspend fun deleteUserSingleRepository(userSingleRepositoryDownloaded: UserSingleRepositoryDownloaded)
}