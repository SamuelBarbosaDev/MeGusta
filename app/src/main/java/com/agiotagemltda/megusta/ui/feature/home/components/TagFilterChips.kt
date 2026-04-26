package com.agiotagemltda.megusta.ui.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.agiotagemltda.megusta.domain.model.PostOrder
import com.agiotagemltda.megusta.ui.feature.home.HomeViewModel


@Composable
fun TagFilterChips(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Row{
//        IconButton(
//            onClick = {
//                navController.navigate("manage_tags")
//            },
//            Modifier.padding(
//                top = 16.dp,
//                bottom = 16.dp,
//                start = 16.dp
//            )
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Settings,
//                contentDescription = "Gerenciar tags"
//            )
//        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 16.dp,
            )
        ) {
            // Ícone de Gerenciar Tags (já existente)
            IconButton(onClick = { navController.navigate("manage_tags") }) {
                Icon(Icons.Default.Settings, contentDescription = "Tags")
            }

            // --- NOVO COMPONENTE DE ORDENAÇÃO ---
            var showMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                }

                DropdownMenu (
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mais recentes") },
                        onClick = { viewModel.setOrder(PostOrder.ID_DESC); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Mais antigos") },
                        onClick = { viewModel.setOrder(PostOrder.ID_ASC); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Nome (A-Z)") },
                        onClick = { viewModel.setOrder(PostOrder.NAME_ASC); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Nome (Z-A)") },
                        onClick = { viewModel.setOrder(PostOrder.NAME_DESC); showMenu = false }
                    )
                }
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(tags) { tag ->
                val isSelectTag = selectedTag == tag

                FilterChip(
                    selected = selectedTag == tag,
                    onClick = {
                        val validationTag = if (isSelectTag) "Todos" else tag
                        onTagSelected(validationTag)
                    },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }
    }
}