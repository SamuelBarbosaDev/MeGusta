package com.agiotagemltda.megusta.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import kotlinx.serialization.Serializable


@Serializable
data class PostWithTags(
    @Embedded
    val post: PostEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PostTagCrossRef::class,
            parentColumn = "postId",
            entityColumn = "tagId"
        )
    )
    val tag: List<TagsEntity>
)
