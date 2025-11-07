package com.agiotagemltda.megusta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
@Entity(tableName = "tags")
data class TagsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
