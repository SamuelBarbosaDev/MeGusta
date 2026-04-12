package com.agiotagemltda.megusta.ui.feature.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.repository.PostRepository
import com.agiotagemltda.megusta.ui.feature.postform.PostFormUiState
import com.agiotagemltda.megusta.ui.feature.postform.PostFormViewModelContract
import com.agiotagemltda.megusta.ui.feature.postform.utils.copyImageToInternalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val repository: PostRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), PostFormViewModelContract {

    private val _uiState = MutableStateFlow(PostFormUiState())
    override val uiState: StateFlow<PostFormUiState> = _uiState.asStateFlow()

    init{
        repository.getAllTagsFlow
            .onEach { tags ->
                _uiState.update { it.copy(allExistingTags = tags) }
            }
            .launchIn(viewModelScope)
    }

    // Métodos da interface
    override fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    override fun updateTags(tags: String) = _uiState.update { it.copy(tagsInput = tags) }
    override fun updateUrl(url: String) = _uiState.update { it.copy(url = url) }
    override fun updateImage(image: String) = _uiState.update { it.copy(image = image) }
    override fun updateImageUri(uri: Uri?) = _uiState.update { it.copy(imageUri = uri) }
    override fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    override fun updateRating(newRating: Int) = _uiState.update { it.copy(rating = newRating) }

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

            repository.insertPostWithTags(
                name = state.name,
                notes = state.notes,
                url = state.url,
                image = imagePath,
                tags = tags
            )

            _uiState.update { it.copy(isSaved = true, isLoading = false) }
        }
    }

    override fun togglePreviewMode() {
        _uiState.update { it.copy(isPreviewMode = !it.isPreviewMode) }
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
}

//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.agiotagemltda.megusta.data.repository.PostRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//
//@HiltViewModel
//class AddPostViewModel @Inject constructor(
//    private val repository: PostRepository
//): ViewModel(){
//
//    fun savePost(
//        name: String,
//        tagsInput: String,
//        url: String,
//        notes: String,
//        image: String
//    ){
//        if(name.isBlank()) return
//
//        viewModelScope.launch {
//            val tags = tagsInput.split(",")
//                .map { it.trim() }
//                .filter{it.isNotBlank()}
//
//            repository.insertPostWithTags(
//                name = name,
//                notes = notes,
//                url = url,
//                image = image,
//                tags = tags
//            )
//        }
//    }
//}