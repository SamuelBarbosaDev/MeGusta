package com.agiotagemltda.megusta.ui.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.agiotagemltda.megusta.R
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
    val focusManager = LocalFocusManager.current
    val isSelectionMode = uiState.isSelectionMode
    val selectedCount = uiState.selectedPostIds.size
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showOverflowMenu by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.writeExportFile(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.readImportFile(it) }
    }

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
                    actions = {
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
            else{
                TopAppBar(
                    title = {},
                    actions = {
                        Row(
                            Modifier.padding(top = 8.dp)
                        ){
                            SearchBarField(
                                query = searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            )
                            Box {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.menu_export)) },
                                        leadingIcon = { Icon(Icons.Default.Share, null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            exportLauncher.launch("me_gusta_backup.json")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(R.string.menu_import)) },
                                        leadingIcon = { Icon(Icons.Default.FileDownload, null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            importLauncher.launch("application/json")
                                        }
                                    )
                                }
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
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // <- Isso remove o foco e fecha o teclado
                })}
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
        ) {
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
    AnimatedContent(targetState = isSearchVisible, label = "") { visible ->
        if (visible) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
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
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSearchIconClick) {
                    Icon(Icons.Default.Search, contentDescription = "Abrir busca")
                }
            }
        }
    }
}