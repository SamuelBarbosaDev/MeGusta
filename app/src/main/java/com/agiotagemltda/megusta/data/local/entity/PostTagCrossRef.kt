package com.agiotagemltda.megusta.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable


@Serializable
@Entity(
    tableName = "post_tag_cross_ref",
    primaryKeys = ["postId", "tagId"],
    indices = [
        Index("postId"),
        Index("tagId")
    ]
)
data class PostTagCrossRef (
    val postId: Long,
    val tagId: Long
)
