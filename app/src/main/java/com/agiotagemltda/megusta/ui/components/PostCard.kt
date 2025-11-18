package com.agiotagemltda.megusta.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.agiotagemltda.megusta.R
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import com.agiotagemltda.megusta.ui.feature.home.HomeViewModel
import java.io.File

@Composable
fun PostCard(
    post: PostEntity,
    tags: List<String>,
    viewModel: HomeViewModel,
    navController: NavController,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsState().value
    val isSelectionMode = uiState.isSelectionMode
    val isNavigating = uiState.isNavigating

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .animateContentSize()
            // ← AQUI É A MÁGICA: o comportamento muda conforme o modo
            .pointerInput(post.id, isSelectionMode) {
                detectTapGestures(
                    onLongPress = {
                        if (!isSelectionMode) {
                            viewModel.enterSelectionMode()
                            viewModel.togglePostSelection(post.id)
                        }
                    },
                    onTap = {
                        if (isSelectionMode) {
                            // Em modo seleção → sempre toggle
                            viewModel.togglePostSelection(post.id)
                        } else if (!isNavigating) {
                            // Fora do modo → abre edição
                            viewModel.onEditPostClicked(post.id)
                        }
                    }
                )
            }
    ) {
        AsyncImage(
            model = when {
                post.image.startsWith("http") -> post.image
                post.image.isNotBlank() -> File(post.image)
                else -> R.drawable.nano_machine
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )

        // Overlay escuro com título e notas
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = post.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = post.notes,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tags no canto superior direito
        if (tags.isNotEmpty()) {
            LazyRow(modifier = Modifier.align(Alignment.TopEnd)) {
                items(tags.take(3)) { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag, fontSize = 10.sp) },
                        modifier = Modifier.padding(end = 4.dp, top = 8.dp)
                    )
                }
                if (tags.size > 3) {
                    item {
                        Text("+${tags.size - 3}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Ícone de check quando selecionado
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selecionado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(32.dp)
            )
        }
    }
}