package com.agiotagemltda.megusta.ui.feature.home

import android.R
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.ui.feature.home.components.PostList
import com.agiotagemltda.megusta.ui.feature.home.components.TagFilterChips
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isSelectionMode = uiState.isSelectionMode
    val selectedCount = uiState.selectedPostIds.size

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeScreenEvent.NavigateToEdit -> {
                    val json = Json.encodeToString(PostWithTags.serializer(), event.postWithTags)
                    navController.navigate("edit_post/${event.postId}?post_data=${Uri.encode(json)}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        TextButton(onClick = { viewModel.exitSelectionMode() }){
                            Icon(
                                Icons.Default.Close,
                                "Sair",
                                modifier = Modifier
                                    .padding(end = 2.dp)
                            )
                            Text(
                                "$selectedCount Selecionado(s)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                    },
//                    navigationIcon = {
//                        Icon(Icons.Default.Close, "Sair")
//                    },
                    actions = {
                        if (uiState.posts.isNotEmpty()) {
                            Row (verticalAlignment = Alignment.CenterVertically){
                                Text(
                                    "Todos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Checkbox(
                                    checked = uiState.selectedPostIds.size == uiState.posts.size,
                                    onCheckedChange = { change ->
                                        if (change){
                                            viewModel.selectAllPosts()
                                        } else {
                                            viewModel.unselectAllPost()
                                        }
                                    }
                                )

                            }
                            }
                    }

                )
            }

        },
        floatingActionButton = {
            AnimatedContent(targetState = isSelectionMode) { selectionMode ->
                if (selectionMode) {
                    FloatingActionButton(
                        onClick = { viewModel.deleteSelectedPosts() },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Icon(Icons.Default.Delete, "Excluir")
                    }
                } else {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("add_post")
                        }
                    ) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
        ) {
            // Filtros por tag
            TagFilterChips(
                tags = listOf("Todos") + uiState.tags,
                selectedTag = uiState.selectedTag ?: "Todos",
                onTagSelected = { viewModel.filterByTag(it) }
            )
            // Lista de posts
            PostList(
                posts = uiState.posts,
                isLoading = uiState.isLoading,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
