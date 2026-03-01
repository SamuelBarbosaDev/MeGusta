package com.agiotagemltda.megusta.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController


@Composable
fun TagFilterChips(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    navController: NavController
) {
    Row{
        IconButton(
            onClick = {
                navController.navigate("manage_tags")
            },
            Modifier.padding(
                top = 16.dp,
                bottom = 16.dp,
                start = 16.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Gerenciar tags"
            )
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