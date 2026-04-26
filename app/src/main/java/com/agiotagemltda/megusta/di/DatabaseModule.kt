package com.agiotagemltda.megusta.di

import android.content.Context
import androidx.room.Room
import com.agiotagemltda.megusta.data.local.AppDatabase
import com.agiotagemltda.megusta.data.local.dao.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            AppDatabase::class.java,
            "megusta_database"
        )
//            .fallbackToDestructiveMigration(dropAllTables = true)<- REMOVA OU COMENTE ESTA LINHA
//            .addMigrations(MIGRATION_1_2) // <- VAMOS CRIAR ISSO AGORA
            .build()
    }

    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao = database.postDao()
}

//val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(db: SupportSQLiteDatabase) {
//        // Comando SQL para adicionar a coluna sem apagar a tabela
//        db.execSQL("ALTER TABLE posts ADD COLUMN rating INTEGER NOT NULL DEFAULT 0")
//    }
//}