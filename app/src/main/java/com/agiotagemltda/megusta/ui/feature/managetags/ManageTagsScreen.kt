package com.agiotagemltda.megusta.ui.feature.managetags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fitInside
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agiotagemltda.megusta.data.local.entity.TagsEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTagsScreen(
    navController: NavController,
    viewModel: ManageTagsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTags = uiState.selectedTagIds
    val hasSelection = selectedTags.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Tags") },
                actions = {
                    if (hasSelection) {
                        Text(
                            text = "${selectedTags.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasSelection) {
                FloatingActionButton(
                    onClick = { viewModel.deleteSelectedTags() },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, "Excluir")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.tags.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues), Alignment.Center
            ) {
                Text("Nenhuma tag criada ainda", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                items(uiState.tags, key = {it.id}){ tag ->
                    TagItem(
                        tag = tag,
                        isSelected = tag.id in selectedTags,
                        onClick = { viewModel.toggleTagSelection(tag.id)}
                    )
                }
            }
        }

        uiState.successMessage?.let{ message ->
            LaunchedEffect(message) {
                viewModel.clearSuccessMessage()
            }
        }
    }
}


@Composable
private fun TagItem(
    tag: TagsEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card (
        colors = CardDefaults.cardColors(
            contentColor = if(isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(64.dp)
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ){
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            if (isSelected){
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

