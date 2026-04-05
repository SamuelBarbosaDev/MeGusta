package com.agiotagemltda.megusta.ui.feature.postform

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.repository.PostRepository
import com.agiotagemltda.megusta.ui.feature.postform.utils.copyImageToInternalStorage
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.agiotagemltda.megusta.data.local.entity.TagsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


data class PostFormUiState(
    val postId: Long = 0,
    val name: String = "",
    val tagsInput: String = "",
    val url: String = "",
    val image: String = "",
    val imageUri: Uri? = null,
    val notes: String = "",
    val rating: Int = 0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isPreviewMode: Boolean = false,
    val allAvailableTags: List<String> = emptyList(),
    val allExistingTags: List<String> = emptyList(), // Novas tags para sugestão
    val newTagInput: String = "" // Texto que o usuário está digitando
)


@HiltViewModel(assistedFactory = PostFormViewModel.Factory::class)
class PostFormViewModel @AssistedInject constructor(
    private val repository: PostRepository,
    @Assisted private val postId: Long = 0L,
    @ApplicationContext private val context: Context
) : ViewModel(), PostFormViewModelContract {
    private val _uiState = MutableStateFlow(PostFormUiState())
    override val uiState: StateFlow<PostFormUiState> = _uiState.asStateFlow()

    init {
        if (postId > 0) loadPost(postId)
//        repository.getAllTagsFlow
//            .onEach { tags ->
//                _uiState.update {
//                    it.copy(allAvailableTags = tags)
//                }
//            }
//            .launchIn(viewModelScope)
        repository.getAllTagsFlow
            .onEach { tags ->
                _uiState.update {
                    it.copy(allExistingTags = tags) // Preenche a lista de sugestões
                }
            }
            .launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(postId: Long): PostFormViewModel
    }

//    private fun observeTags() {
//        viewModelScope.launch {
//            repository.getAllTagsFlow // Certifique-se que no repositório este Flow retorna List<String>
//                .catch { e ->
//                    _uiState.update { it.copy(error = e.message) }
//                }
//                .collect { tags ->
//                    _uiState.update { it.copy(allAvailableTags = tags) }
//                }
//        }
//    }

    fun loadPost(postId: Long) {
        if (postId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getPostById(postId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collectLatest { postWithTags ->
                    postWithTags?.let { pwt ->
                        _uiState.update {
                            it.copy(
                                postId = pwt.post.id,
                                name = pwt.post.name,
                                tagsInput = pwt.tag.joinToString(", ") { tag -> tag.name },
                                url = pwt.post.url,
                                image = pwt.post.image,
                                notes = pwt.post.notes,
                                rating = pwt.post.rating,
                                isLoading = false
                            )
                        }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    override fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    override fun updateTags(tags: String) = _uiState.update { it.copy(tagsInput = tags) }
    override fun updateUrl(url: String) = _uiState.update { it.copy(url = url) }
    override fun updateImage(image: String) = _uiState.update { it.copy(image = image) }
    override fun updateImageUri(uri: Uri?) = _uiState.update { it.copy(imageUri = uri) }
    override fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    override fun updateRating(newRating: Int) = _uiState.update { it.copy(rating = newRating) }
    // Dentro do seu ViewModel
    override fun togglePreviewMode() {
        _uiState.update { it.copy(isPreviewMode = !it.isPreviewMode) }
    }

    // Atualiza o que o usuário digita no campo de nova tag
    override fun onNewTagContentChange(newValue: String) {
        _uiState.update { it.copy(newTagInput = newValue) }
    }

    // Quando o usuário clica em um Chip ou digita e aperta "Enter"
    override fun toggleTagSelection(tag: String) {
        _uiState.update { state ->
            // Pega as tags atuais (que estão no campo de texto tagsInput separadas por vírgula)
            val currentList = state.tagsInput.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableList()

            if (currentList.contains(tag)) {
                currentList.remove(tag) // Deseleciona
            } else {
                currentList.add(tag) // Seleciona
            }

            // Devolve para o campo de texto formatado
            state.copy(tagsInput = currentList.joinToString(", "))
        }
    }

    // Função para o botão "+" ou Tecla Enter do teclado
    override fun addNewTag() {
        val tag = _uiState.value.newTagInput.trim()
        if (tag.isNotBlank()) {
            toggleTagSelection(tag)
            _uiState.update { it.copy(newTagInput = "") } // Limpa o campo após adicionar
        }
    }

    override fun savePostTogglePreviewMode() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val tags = state.tagsInput.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val imagePath = if (state.imageUri != null) {
                copyImageToInternalStorage(context, state.imageUri!!) ?: state.image
            } else {
                state.image // mantém URL ou caminho antigo
            }

            if (state.postId > 0) {
                repository.updatePostWithTags(
                    postId = state.postId,
                    name = state.name,
                    notes = state.notes,
                    url = state.url,
                    image = imagePath, // ← AQUI!
                    tags = tags,
                    rating = state.rating
                )
            } else {
                repository.insertPostWithTags(
                    name = state.name,
                    notes = state.notes,
                    url = state.url,
                    image = imagePath, // ← AQUI!
                    tags = tags,
                    rating = state.rating
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    override fun savePost() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val tags = state.tagsInput.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val imagePath = if (state.imageUri != null) {
                copyImageToInternalStorage(context, state.imageUri!!) ?: state.image
            } else {
                state.image // mantém URL ou caminho antigo
            }

            if (state.postId > 0) {
                repository.updatePostWithTags(
                    postId = state.postId,
                    name = state.name,
                    notes = state.notes,
                    url = state.url,
                    image = imagePath, // ← AQUI!
                    tags = tags,
                    rating = state.rating
                )
            } else {
                repository.insertPostWithTags(
                    name = state.name,
                    notes = state.notes,
                    url = state.url,
                    image = imagePath, // ← AQUI!
                    tags = tags,
                    rating = state.rating
                )
            }

            _uiState.update { it.copy(isSaved = true, isLoading = false) }
        }
    }
}