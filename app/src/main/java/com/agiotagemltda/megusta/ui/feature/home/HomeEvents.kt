package com.agiotagemltda.megusta.ui.feature.home

import com.agiotagemltda.megusta.data.local.entity.PostWithTags

sealed class HomeScreenEvent{
    data class NavigateToEdit(val postId: Long, val postWithTags: PostWithTags): HomeScreenEvent()
}