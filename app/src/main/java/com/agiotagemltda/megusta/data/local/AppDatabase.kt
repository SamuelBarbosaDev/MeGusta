package com.agiotagemltda.megusta.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agiotagemltda.megusta.data.local.dao.PostDao
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.data.local.entity.PostTagCrossRef
import com.agiotagemltda.megusta.data.local.entity.TagsEntity

@Database(
    entities = [
        PostEntity::class,
        TagsEntity::class,
        PostTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase(){
    abstract fun postDao(): PostDao
}