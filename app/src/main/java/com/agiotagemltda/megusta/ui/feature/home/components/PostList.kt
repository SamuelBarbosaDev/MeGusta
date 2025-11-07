package com.agiotagemltda.megusta.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.ui.components.PostCard
import com.agiotagemltda.megusta.ui.feature.home.HomeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun PostList(
    posts: List<PostWithTags>,
    isLoading: Boolean,
    viewModel: HomeViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    if (isLoading){
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ){
            CircularProgressIndicator()
        }
        return

    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        contentPadding = PaddingValues(20.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(posts, key = { it.post.id }) { posts ->
            val isSelected = uiState.selectedPostIds.contains(posts.post.id)
            PostCard(
                post = posts.post,
                tags = posts.tag.map { it.name },
                viewModel = viewModel,
                navController = navController,
                isSelected =isSelected
            )
        }
    }
}