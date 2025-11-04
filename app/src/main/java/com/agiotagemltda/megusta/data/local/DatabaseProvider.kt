package com.agiotagemltda.megusta.data.local

import android.content.Context
import androidx.room.Room


object DatabaseProvider{
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase{
        return instance ?: synchronized(this){
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "megusta_database"
            ).build()
            instance = db
            db
        }
    }
}