package com.agiotagemltda.megusta.ui.feature.postform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.agiotagemltda.megusta.ui.feature.home.components.rememberImagePicker
import com.agiotagemltda.megusta.ui.theme.LinkBlue
import com.agiotagemltda.megusta.ui.theme.LinkSapphire
import com.agiotagemltda.megusta.ui.theme.LinkSky
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.File
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFormScreen(
    navController: NavController,
    viewModel: PostFormViewModelContract,
    isEditMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pickImage = rememberImagePicker { uri -> viewModel.updateImageUri(uri) }

    // Fecha a tela após salvar com sucesso
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isPreviewMode) "Visualização" else if (isEditMode) "Editar" else "Novo Post") },
                actions = {
                    // Botão para alternar entre Edição e Preview
                    IconButton(onClick = { viewModel.togglePreviewMode() }) {
                        Icon(
                            imageVector = if (uiState.isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = "Alternar Preview"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Só mostra o botão de salvar se não estiver no modo preview
            if (!uiState.isPreviewMode) {
                FloatingActionButton(onClick = { viewModel.savePost() }) {
                    Icon(Icons.Default.Done, contentDescription = "Salvar")
                }
            }
        },
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isPreviewMode) {
                // EXIBIÇÃO DO PREVIEW (MARKDOWN)
                MarkdownPreviewLayout(uiState, pickImage)
            } else {
                // FORMULÁRIO DE EDIÇÃO
                EditorLayout(uiState, viewModel, pickImage)
            }
        }
    }
}

@Composable
fun EditorLayout(
    uiState: PostFormUiState,
    viewModel: PostFormViewModelContract,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Área da Imagem
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
                .clickable { onPickImage() }
        ) {
            val imageSource = when {
                uiState.imageUri != null -> uiState.imageUri
                uiState.image.isNotBlank() -> if (uiState.image.startsWith("/")) File(uiState.image) else uiState.image
                else -> null
            }

            if (imageSource != null) {
                AsyncImage(
                    model = imageSource,
                    contentDescription = "Capa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("Toque para selecionar imagem", color = Color.Gray)
                }
            }
        }

        // Campos de Texto
        Column(modifier = Modifier.padding(20.dp)) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

//            OutlinedTextField(
//                value = uiState.tagsInput,
//                onValueChange = { viewModel.updateTags(it) },
//                label = { Text("Tags") },
//                placeholder = { Text("ex: Ação, Anime, Ficção") },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !uiState.isLoading
//            )
            Text(uiState.allAvailableTags.joinToString())
            TagSelector(
                currentTags = uiState.tagsInput,
                availableTags = uiState.allAvailableTags,
                onTagsChange = { viewModel.updateTags(it) },
                isEnabled = !uiState.isLoading
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
                label = { Text("Anotações (Markdown)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun MarkdownPreviewLayout(
    uiState: PostFormUiState,
    onPickImage: () -> Unit
    ) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
                .clickable { onPickImage() }
        ) {
            val imageSource = when {
                uiState.imageUri != null -> uiState.imageUri
                uiState.image.isNotBlank() -> if (uiState.image.startsWith("/")) File(uiState.image) else uiState.image
                else -> null
            }

            if (imageSource != null) {
                AsyncImage(
                    model = imageSource,
                    contentDescription = "Capa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("Toque para selecionar imagem", color = Color.Gray)
                }
            }
        }
        if (uiState.name.isNotBlank()) {
            Text(text = uiState.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Tags: ${uiState.tagsInput}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            FilledIconButton(
                onClick = {
                    // Tenta abrir a URL. É bom garantir que comece com http/https
                    val formattedUrl = if (!uiState.url.startsWith("http")) {
                        "https://${uiState.url}"
                    } else uiState.url

                    try {
                        uriHandler.openUri(formattedUrl)
                    } catch (e: Exception) {
                        // Caso a URL seja inválida, você pode disparar um log ou Toast
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = LinkBlue.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Abrir Link Externo"
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        }

        // Renderização do Markdown
        MarkdownText(
            markdown = uiState.notes.ifBlank { "_Nenhuma anotação disponível._" },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TagSelector(
//    currentTags: String,
//    availableTags: List<String>,
//    onTagsChange: (String) -> Unit,
//    isEnabled: Boolean
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    // Filtra as sugestões baseadas no que o usuário está digitando (após a última vírgula)
//    val lastTag = currentTags.split(",").last().trim()
//    val filteredOptions = availableTags.filter {
//        it.contains(lastTag, ignoreCase = true) && it.isNotBlank()
//    }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded && filteredOptions.isNotEmpty(),
//        onExpandedChange = { expanded = it }
//    ) {
//        OutlinedTextField(
//            value = currentTags,
//            onValueChange = {
//                onTagsChange(it)
//                expanded = true
//            },
//            label = { Text("Tags (separe por vírgula)") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .menuAnchor(), // Importante para posicionar o menu
//            enabled = isEnabled,
//            placeholder = { Text("Ex: Ação, Drama...") }
//        )
//
//        // O Menu de sugestões
//        ExposedDropdownMenu(
//            expanded = expanded && filteredOptions.isNotEmpty(),
//            onDismissRequest = { expanded = false }
//        ) {
//            filteredOptions.forEach { selectionOption ->
//                DropdownMenuItem(
//                    text = { Text(selectionOption) },
//                    onClick = {
//                        // Lógica para adicionar a tag selecionada à lista de strings
//                        val tagsList = currentTags.split(",").map { it.trim() }.toMutableList()
//                        if (tagsList.isNotEmpty()) {
//                            tagsList[tagsList.size - 1] = selectionOption
//                        } else {
//                            tagsList.add(selectionOption)
//                        }
//                        onTagsChange(tagsList.joinToString(", ") + ", ")
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    currentTags: String,
    availableTags: List<String>,
    onTagsChange: (String) -> Unit,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val lastTag = currentTags.split(",").last().trim()
    val filteredOptions = availableTags.filter {
        it.contains(lastTag, ignoreCase = true)
    }

    // O ExposedDropdownMenuBox precisa envolver o TextField e o Menu
    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled, // Removi o check de lista vazia para teste
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentTags,
            onValueChange = {
                onTagsChange(it)
                expanded = true
            },
            label = { Text("Tags (separe por vírgula)") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // SE NÃO TIVER ISSO, O MENU NÃO APARECE
            enabled = isEnabled,
            placeholder = { Text("Ex: Ação, Drama...") }
        )

        // Se houver opções, mostramos o menu
        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            val tagsList = currentTags.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .toMutableList()

                            if (tagsList.isNotEmpty() && currentTags.endsWith("").not()) {
                                // Substitui a última palavra pela seleção
                                tagsList[tagsList.size - 1] = selectionOption
                            } else {
                                tagsList.add(selectionOption)
                            }

                            // Adiciona vírgula e espaço após selecionar para facilitar a próxima tag
                            onTagsChange(tagsList.joinToString(", ") + ", ")
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
