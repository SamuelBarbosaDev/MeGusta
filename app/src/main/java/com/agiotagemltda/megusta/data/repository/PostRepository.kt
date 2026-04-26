package com.agiotagemltda.megusta.data.repository

import com.agiotagemltda.megusta.data.local.dao.PostDao
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.local.entity.TagsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class PostRepository(private val postDao: PostDao){
    val allPostsFlow: Flow<List<PostWithTags>> = postDao.getAllPostsWithTagsFlow()

    val allASCPostsFlow: Flow<List<PostWithTags>> = postDao.getASCAllPostsWithTagsFlow()

    val allABCPostsFlow: Flow<List<PostWithTags>> = postDao.getABCAllPostsWithTagsFlow()

    val allDescABCPostsFlow: Flow<List<PostWithTags>> = postDao.getDescABCAllPostsWithTagsFlow()

    val getAllTagsFlow: Flow<List<String>> = postDao.getAllTagsFlow()

    fun getPostByTag(tag: String): Flow<List<PostWithTags>> =
        postDao.getPostsByTag(tag)

    fun getPostById(id: Long): Flow<PostWithTags?> =
        postDao.getPostByIdFlow(id)

    suspend fun deletePostTags(postId: Long) =
        postDao.deletePostTags(postId)


    fun searchTagsFlow(query: String): Flow<List<TagsEntity>> =
        postDao.searchTags(query)

    suspend fun insertPostWithTags(
        name: String,
        notes: String,
        url: String,
        image: String,
        tags: List<String>,
        rating: Int = 0
    ) {
        val post = PostEntity(name = name, notes = notes, url = url, image = image, rating = rating)
        postDao.insertPostWithTags(post, tags) // ← INSERT
    }

    suspend fun updatePostWithTags(
        postId: Long,
        name: String,
        notes: String,
        url: String,
        image: String,
        tags: List<String>,
        rating: Int
    ) {
        val post = PostEntity(id = postId, name = name, notes = notes, url = url, image = image, rating = rating)
        postDao.updatePostWithTags(post, tags)
    }

    suspend fun deletePost(postId: Long){
        postDao.deletePost(postId)
    }

    suspend fun deleteTagById(tagId: Long){
        postDao.deleteTagAndCrossRefs(tagId)
    }

    fun getAllTagsWithIdFlow(): Flow<List<TagsEntity>> =
        postDao.getAllTagsWithIdFLow()

    suspend fun exportAllPostsToJson(): String {
        val allData = postDao.getAllPostsWithTagsStatic()
        return Json.encodeToString(allData)
    }

    suspend fun importPostsFromJson(jsonString: String) {
        val data = Json.decodeFromString<List<PostWithTags>>(jsonString)
        data.forEach { item ->
            // Criamos um novo PostEntity baseado no importado (para gerar novo ID e não conflitar)
            val postToInsert = item.post.copy(id = 0)
            postDao.insertPostWithTags(postToInsert, item.tag.map { it.name })
        }
    }
}