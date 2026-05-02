package com.agiotagemltda.megusta.data.repository

import android.content.Context
import com.agiotagemltda.megusta.data.local.dao.PostDao
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.local.entity.TagsEntity
import com.agiotagemltda.megusta.domain.model.PostOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class PostRepository(private val postDao: PostDao){
    fun getPosts(order: PostOrder): Flow<List<PostWithTags>> {
        return when (order) {
            PostOrder.ID_DESC -> postDao.getAllPostsWithTagsFlow()
            PostOrder.ID_ASC -> postDao.getASCAllPostsWithTagsFlow()
            PostOrder.NAME_ASC -> postDao.getABCAllPostsWithTagsFlow()
            PostOrder.NAME_DESC -> postDao.getDescABCAllPostsWithTagsFlow()
        }
    }

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

    suspend fun importFromZip(inputStream: InputStream, context: Context) {
        val zipInputStream = ZipInputStream(inputStream)
        var entry: ZipEntry? = zipInputStream.getNextEntry()
        var jsonContent = ""
        val imageMap = mutableMapOf<String, ByteArray>()

        while (entry != null) {
            when {
                entry.name == "backup.json" -> {
                    jsonContent = zipInputStream.bufferedReader().readText()
                }
                entry.name.startsWith("images/") -> {
                    val fileName = entry.name.removePrefix("images/")
                    if (fileName.isNotEmpty()) {
                        imageMap[fileName] = zipInputStream.readBytes()
                    }
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.getNextEntry()
        }

        if (jsonContent.isNotEmpty()) {
            val data = Json.decodeFromString<List<PostWithTags>>(jsonContent)
            val imagesDir = File(context.filesDir, "images").apply { mkdirs() }

            data.forEach { item ->
                // Recupera a imagem do mapa e salva no disco interno do novo aparelho
                val oldPath = item.post.image
                val fileName = oldPath.substringAfterLast("/")

                val newImagePath = imageMap[fileName]?.let { bytes ->
                    val newFile = File(imagesDir, fileName)
                    newFile.writeBytes(bytes)
                    newFile.absolutePath
                } ?: ""

                val postToInsert = item.post.copy(id = 0, image = newImagePath)
                postDao.insertPostWithTags(postToInsert, item.tag.map { it.name })
            }
        }
    }
}