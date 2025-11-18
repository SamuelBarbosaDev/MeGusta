package com.agiotagemltda.megusta.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.data.local.entity.PostTagCrossRef
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.local.entity.TagsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao{
    @Transaction
    @Query("SELECT * FROM posts")
    fun getAllPostsWithTagsFlow(): Flow<List<PostWithTags>>

    @Insert
    suspend fun insertPost(post: PostEntity): Long

    @Insert
    suspend fun insertTag(tag: TagsEntity): Long

    @Insert
    suspend fun insertCrossRef(crossRef: PostTagCrossRef)

    @Query("SELECT id FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagIdByName(name: String): Long?

    // Função utilitária para inserir post + tags
    @Transaction
    suspend fun insertPostWithTags(post: PostEntity, tags: List<String>){
        val postId = insertPost(post)

        tags.forEach { tag ->
            var tagId = getTagIdByName(tag)
            if (tagId == null){
                tagId = insertTag(TagsEntity(name = tag))
            }
            insertCrossRef(PostTagCrossRef(postId = postId, tagId = tagId))
        }
    }

    @Transaction
    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostWithTagsByIdFlow(id: Long): Flow<PostWithTags?>

    @Transaction
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query")
    fun searchTags(query: String): Flow<List<TagsEntity>>

    @Query("SELECT DISTINCT name FROM tags")
    fun getAllTagsFlow(): Flow<List<String>>

    @Transaction
    @Query("""
        SELECT * FROM posts
        WHERE id IN (
            SELECT postId FROM post_tag_cross_ref
            WHERE tagId IN (SELECT id FROM tags WHERE name = :tag)
        )
    """)
    fun getPostsByTag(tag: String): Flow<List<PostWithTags>>

    @Transaction
    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostByIdFlow(id: Long): Flow<PostWithTags?>

    @Query("DELETE FROM post_tag_cross_ref WHERE postId = :postId")
    suspend fun deletePostTags(postId: Long)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Transaction
    suspend fun updatePostWithTags(post: PostEntity, tags: List<String>) {
        updatePost(post)
        deletePostTags(post.id)
        tags.forEach { tagName ->
            var tagId = getTagIdByName(tagName)
            if (tagId == null) {
                tagId = insertTag(TagsEntity(name = tagName))
            }
            insertCrossRef(PostTagCrossRef(postId = post.id, tagId = tagId))
        }
    }

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("DELETE FROM post_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteCrossRefByTagId(tagId: Long)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Transaction
    suspend fun deleteTagAndCrossRefs(tagId: Long){
        deleteCrossRefByTagId(tagId)
        deleteTagById(tagId)
    }

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsWithIdFLow(): Flow<List<TagsEntity>>
}
