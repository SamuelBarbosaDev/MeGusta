package com.agiotagemltda.megusta.ui.feature.managetags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.local.entity.TagsEntity
import com.agiotagemltda.megusta.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageTagsUiState(
    val tags: List<TagsEntity> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val successMessage: String? = null
)

@HiltViewModel
class ManageTagsViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageTagsUiState())
    val uiState: StateFlow<ManageTagsUiState> = _uiState.asStateFlow()

    init {
        repository.getAllTagsWithIdFlow()
            .onEach { tags ->
                _uiState.update {
                    it.copy(tags = tags, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleTagSelection(tagId: Long) {
        _uiState.update { state ->
            val selected = state.selectedTagIds
            state.copy(selectedTagIds = if (tagId in selected) selected - tagId else selected + tagId)
        }
    }

    fun deleteSelectedTags(){
        viewModelScope.launch {
            val idsToDelete = _uiState.value.selectedTagIds.toList()
            idsToDelete.forEach { tagId ->
                repository.deleteTagById(tagId)
            }
            _uiState.update {
                it.copy(
                    selectedTagIds = emptySet(),
                    successMessage = "Tags excluídas com sucesso!"
                )
            }
        }
    }
    fun clearSuccessMessage(){
        _uiState.update { it.copy(successMessage = null) }
    }

}