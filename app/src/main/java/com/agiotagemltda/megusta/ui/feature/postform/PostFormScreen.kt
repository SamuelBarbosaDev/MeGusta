package com.agiotagemltda.megusta.ui.feature.postform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.agiotagemltda.megusta.R
import com.agiotagemltda.megusta.ui.components.MeGustaTextField
import com.agiotagemltda.megusta.ui.feature.home.components.rememberImagePicker
import com.agiotagemltda.megusta.ui.theme.LinkBlue
import com.agiotagemltda.megusta.ui.theme.LinkSky
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.File

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
                    IconButton(onClick = {
                        viewModel.togglePreviewMode()
                        viewModel.savePostTogglePreviewMode()
                    }) {
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
            RatingButtons(
                selectedRating = uiState.rating,
                onRatingSelected = {viewModel.updateRating(it)}
            )

            MeGustaTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = stringResource(R.string.label_name) // Usando strings.xml!
            )

            Spacer(modifier = Modifier.height(8.dp))

            TagSelectorSection(
                uiState = uiState,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(8.dp))

            MeGustaTextField(
                value = uiState.url,
                onValueChange = { viewModel.updateUrl(it) },
                label = "URL",
                placeholder = "https://..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            MeGustaTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = "Anotações",
                singleLine = false,
                minLines = 5 // Perfeito para o Markdown
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
            if (uiState.rating != 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart) // Posiciona no canto inferior direito
                        .padding(12.dp) // Afasta um pouco da borda
                        .size(48.dp) // Tamanho do círculo
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = CircleShape // Formato circular
                        )
                        .border(
                            width = 1.5.dp, // Espessura sutil
                            color = Color.White.copy(alpha = 0.3f), // Mesma cor e alpha das tags
                            shape = CircleShape // Fundamental: a borda deve seguir o formato do círculo
                        ),
                    contentAlignment = Alignment.Center // Centraliza o ícone dentro do círculo
                ) {
                    Icon(
                        imageVector = if (uiState.rating == 1) Icons.Filled.ThumbUp else Icons.Filled.ThumbDown,
                        contentDescription = "Rating",
                        tint = if (uiState.rating == 1) LinkSky else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
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

@Composable
fun RatingButtons(
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        // Botão Gostei
        IconButton(onClick = { onRatingSelected(if (selectedRating == 1) 0 else 1) }) {
            Icon(
                imageVector = if (selectedRating == 1) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Gostei",
                tint = if (selectedRating == 1) Color.White else Color.White //LinkSky else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Botão Não Gostei
        IconButton(onClick = { onRatingSelected(if (selectedRating == 2) 0 else 2) }) {
            Icon(
                imageVector = if (selectedRating == 2) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Não Gostei",
                tint = if (selectedRating == 2) Color.White else Color.White //MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagSelectorSection(uiState: PostFormUiState, viewModel: PostFormViewModelContract) {
    var isExpanded by remember { mutableStateOf(false) }

    // Pegamos o que está escrito agora no post
    val currentPostTags = uiState.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

    // Unimos as tags globais do banco com as que o usuário acabou de digitar para garantir que apareçam
    val allTagsToShow = (uiState.allExistingTags + currentPostTags).distinct().sortedBy { it.lowercase() }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }

        OutlinedTextField(
            value = uiState.newTagInput,
            onValueChange = { viewModel.onNewTagContentChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nova tag...") },
            trailingIcon = {
                IconButton(onClick = { viewModel.addNewTag() }) { Icon(Icons.Default.Add, null) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.addNewTag() }),
            singleLine = true
        )

        AnimatedVisibility(visible = isExpanded) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTagsToShow.forEach { tagName ->
                    val isSelected = currentPostTags.any { it.equals(tagName, ignoreCase = true) }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleTagSelection(tagName) },
                        label = { Text(tagName) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}