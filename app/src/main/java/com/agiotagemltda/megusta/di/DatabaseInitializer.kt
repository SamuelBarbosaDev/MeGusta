package com.agiotagemltda.megusta.di

import com.agiotagemltda.megusta.data.local.AppDatabase
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.di.generatorMovies  // IMPORTA A FUNÇÃO!
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: AppDatabase
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.postDao()
            if (dao.getAllPostsWithTagsFlow().firstOrNull().isNullOrEmpty()) {
                generatorMovies().forEach { post ->
                    dao.insertPostWithTags(
                        post = PostEntity(
                            name = post.name,
                            notes = post.notes,
                            url = post.url,
                            image = post.image
                        ),
                        tags = post.tags
                    )
                }
            }
        }
    }
}