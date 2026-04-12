package com.agiotagemltda.megusta.ui.feature.home

import android.R
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.ui.feature.home.components.PostList
import com.agiotagemltda.megusta.ui.feature.home.components.TagFilterChips
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


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
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Estado local para controlar se a barra de busca está aberta
    var isSearchActive by remember { mutableStateOf(false) }

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
                        TextButton(onClick = { viewModel.exitSelectionMode() }) {
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
//                        if (uiState.isSelectionMode){

//                        }
                        if (uiState.posts.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Todos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Checkbox(
                                    checked = uiState.selectedPostIds.size == uiState.posts.size,
                                    onCheckedChange = { change ->
                                        if (change) {
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
            ExpandableSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                isSearchVisible = isSearchActive,
                onSearchIconClick = { isSearchActive = true },
                onCloseClick = {
                    isSearchActive = false
                    viewModel.onSearchQueryChanged("") // Limpa a busca ao fechar
                }
            )
//            val searchQuery by viewModel.searchQuery.collectAsState() // Adicione essa variável ao ViewModel
//
//            // 2. Campo de Busca
//            SearchBarField(
//                query = searchQuery,
//                onQueryChange = { viewModel.onSearchQueryChanged(it) }
//            )
            // Filtros por tag
            TagFilterChips(
                tags = listOf("Todos") + uiState.tags,
                selectedTag = uiState.selectedTag ?: "Todos",
                onTagSelected = { viewModel.filterByTag(it) },
                navController = navController,
                viewModel = viewModel
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

@Composable
fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar por nome ou tag...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
            }
        },
        shape = RoundedCornerShape(24.dp), // Deixa a barra "redondinha"
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun ExpandableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchVisible: Boolean,
    onSearchIconClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    // Animação suave para expandir/recolher
    AnimatedContent(targetState = isSearchVisible, label = "") { visible ->
        if (visible) {
            // Barra de busca aberta
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotEmpty()) onQueryChange("") else onCloseClick()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
        } else {
            // Apenas o ícone de lupa (pode ficar alinhado com o TagFilterChips)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSearchIconClick) {
                    Icon(Icons.Default.Search, contentDescription = "Abrir busca")
                }
            }
        }
    }
}