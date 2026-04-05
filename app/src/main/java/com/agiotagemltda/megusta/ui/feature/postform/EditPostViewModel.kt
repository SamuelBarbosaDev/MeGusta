package com.agiotagemltda.megusta.ui.feature.postform

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.repository.PostRepository
import com.agiotagemltda.megusta.ui.feature.postform.utils.copyImageToInternalStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@HiltViewModel(assistedFactory = EditPostViewModel.Factory::class)
class EditPostViewModel @AssistedInject constructor(
    private val repository: PostRepository,
    val savedStateHandle: SavedStateHandle,
    @Assisted private val postId: Long,
    @ApplicationContext private val context: Context
) : ViewModel(), PostFormViewModelContract {

    private val _uiState = MutableStateFlow(PostFormUiState())
    override val uiState: StateFlow<PostFormUiState> = _uiState.asStateFlow()

    init {
        val postJson: String? = savedStateHandle["post_data"]
        if (postJson != null){
            try {
                val postWithTags = Json.decodeFromString<PostWithTags>(postJson)
                _uiState.update {
                    it.copy(
                        postId = postWithTags.post.id,
                        name = postWithTags.post.name,
                        tagsInput = postWithTags.tag.joinToString(", ") { it.name },
                        url = postWithTags.post.url,
                        image = postWithTags.post.image,
                        notes = postWithTags.post.notes,
                        rating = postWithTags.post.rating
                    )
                }
            } catch (e: Exception) {
                // fallback
                loadPost(postId)
            }
        } else {
            val postId = savedStateHandle.get<Long>("postId") ?: 0L
            if (postId > 0) loadPost(postId)
        }
        repository.getAllTagsFlow
            .onEach { tags ->
                _uiState.update { it.copy(allExistingTags = tags) }
            }
            .launchIn(viewModelScope)
//        loadPost(postId)
    }

    @AssistedFactory
    interface Factory {
        fun create(postId: Long): EditPostViewModel
    }

    fun loadPost(postId: Long) {
        if (postId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getPostById(postId)
                .collectLatest { postWithTags ->
                    postWithTags?.let { pwt ->
                        _uiState.update {
                            it.copy(
                                postId = pwt.post.id,
                                name = pwt.post.name,
                                tagsInput = pwt.tag.joinToString(", ") { it.name },
                                url = pwt.post.url,
                                image = pwt.post.image,
                                notes = pwt.post.notes,
                                rating = pwt.post.rating,
                                isLoading = false
                            )
                        }
                    } ?: _uiState.update { it.copy(isLoading = false) }
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


    override fun savePost() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tags = state.tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

            val imagePath = if (state.imageUri != null) {
                copyImageToInternalStorage(context, state.imageUri!!) ?: state.image

            } else {
                state.image // mantém URL ou caminho antigo
            }
            repository.updatePostWithTags(
                postId = state.postId,
                name = state.name,
                notes = state.notes,
                url = state.url,
                image = imagePath,
                tags = tags,
                rating = state.rating
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
    // 3. ADICIONE ESTAS FUNÇÕES (copie da PostFormViewModel)
    override fun onNewTagContentChange(newValue: String) {
        _uiState.update { it.copy(newTagInput = newValue) }
    }

    override fun toggleTagSelection(tag: String) {
        _uiState.update { state ->
            val currentList = state.tagsInput.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableList()

            if (currentList.contains(tag)) {
                currentList.remove(tag)
            } else {
                currentList.add(tag)
            }
            state.copy(tagsInput = currentList.joinToString(", "))
        }
    }

    override fun addNewTag() {
        val tag = _uiState.value.newTagInput.trim()
        if (tag.isNotBlank()) {
            toggleTagSelection(tag)
            _uiState.update { it.copy(newTagInput = "") }
        }
    }
}