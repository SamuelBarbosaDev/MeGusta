package com.agiotagemltda.megusta.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
    val isNavigating = uiState.isNavigating
    var longPressConsumed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,                 // sem ripple (opcional)
//                onClick = {
//                    if (uiState.isSelectionMode) {
//                        viewModel.togglePostSelection(post.id)
//                    } else if (!isNavigating) {
//                        viewModel.onEditPostClicked(post.id)
//                    }
//                },
//                onLongClick = {
//                    if (!uiState.isSelectionMode) {
//                        viewModel.enterSelectionMode()
//                        viewModel.togglePostSelection(post.id)
//                    }
//                }
//            )
//            .pointerInput(post.id) {
//                awaitPointerEventScope {
//                    while (true) {
//                        val event = awaitPointerEvent()
//                        val down = event.changes.firstOrNull() ?: continue
//
//                        // 1. Long press
//                        val longPress = down.pressed && down.uptimeMillis - down.previousUptimeMillis > 500L
//                        if (longPress && !uiState.isSelectionMode) {
//                            longPressConsumed = true
//                            viewModel.enterSelectionMode()
//                            viewModel.togglePostSelection(post.id)
//                            down.consume()               // ← consome o toque
//                            continue
//                        }
//
//                        // 2. Tap (apenas se não foi long press)
//                        if (down.pressed && !longPressConsumed) {
//                            val up = waitForUpOrCancellation()
//                            if (up != null) {
//                                if (uiState.isSelectionMode) {
//                                    viewModel.togglePostSelection(post.id)
//                                } else if (!isNavigating) {
//                                    viewModel.onEditPostClicked(post.id)
//                                }
//                            }
//                            longPressConsumed = false   // reseta para próximo toque
//                        }
//                    }
//                }
//            }
            .pointerInput(post.id) {
                detectTapGestures(
                    onLongPress = {
                        if (!uiState.isSelectionMode) {
                            viewModel.enterSelectionMode()
                            viewModel.togglePostSelection(post.id)
                        }
                    },
                    onTap = { offset ->
                        if (uiState.isSelectionMode) {
                            viewModel.togglePostSelection(post.id)
                        } else if (!isNavigating) {
                            viewModel.onEditPostClicked(post.id)
                        }
                    }
                )
            }
            .animateContentSize()
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
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
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
        if (tags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                items(tags.take(3)) { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag, fontSize = 10.sp) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                if (tags.size > 3) {
                    item {
                        Text("+${tags.size - 3}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selecionando",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp)
            )
        }
    }
}