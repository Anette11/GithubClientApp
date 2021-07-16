package com.example.githubclientapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.githubclientapp.util.Constants

@Entity(
    tableName = Constants.TABLE_NAME
)
data class UserSingleRepositoryDownloaded(
    @PrimaryKey
    val downloadedId: Long,
    val fileName: String,
    val path: String
)