package com.agiotagemltda.megusta.domain.model

data class Post(
    val id: Long = 0,
    val name: String = "",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val url: String = "",
    val image: String = ""
)