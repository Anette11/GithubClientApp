package com.example.githubclientapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.githubclientapp.models.UserSingleRepositoryDownloaded
import com.example.githubclientapp.util.Constants

@Database(
    entities = [UserSingleRepositoryDownloaded::class],
    version = Constants.DATABASE_VERSION
)
abstract class ClientDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao

    companion object {
        @Volatile
        private var INSTANCE: ClientDatabase? = null

        fun getClientDatabase(context: Context): ClientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClientDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}