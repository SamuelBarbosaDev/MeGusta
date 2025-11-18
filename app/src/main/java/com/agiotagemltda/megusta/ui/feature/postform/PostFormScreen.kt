package com.agiotagemltda.megusta.ui.feature.postform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.agiotagemltda.megusta.ui.feature.home.components.rememberImagePicker
import java.io.File


@Composable
fun PostFormScreen(
    navController: NavController,
    viewModel: PostFormViewModelContract,
    isEditMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickImage = rememberImagePicker { uri ->
        viewModel.updateImageUri(uri)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.savePost() }
            ) {
                Icon(Icons.Default.Done, contentDescription = "Salvar")
            }
        }
    ) { innerPadding ->
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
//                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .clickable{pickImage()}
//                    .border(2.dp, Color.Gray.copy(0.3f), RoundedCornerShape(12.dp))
            ){
                when{
                    uiState.imageUri != null -> {
                        AsyncImage(
                            model = uiState.imageUri,
                            contentDescription = "Imagem Selecionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.image.isNotBlank() && uiState.image.startsWith("/") -> {
                        val file = File(uiState.image)
                        if (file.exists()){
                            AsyncImage(
                                model = file,
                                contentDescription = "Capa (URL)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = File(uiState.image),
                                contentDescription = "Capa (URL)",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Text("Toque para selecionar imagem", color = Color.Gray)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = {viewModel.updateName(it) },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.tagsInput,
                    onValueChange = { viewModel.updateTags(it) },
                    label = { Text("Tags") },
                    placeholder = { Text("ex: Ação, Anime, Ficção") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.url,
                    onValueChange = { viewModel.updateUrl(it) },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("Anotações") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = !uiState.isLoading,
                    minLines = 4,
                    maxLines = Int.MAX_VALUE,
                )

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}