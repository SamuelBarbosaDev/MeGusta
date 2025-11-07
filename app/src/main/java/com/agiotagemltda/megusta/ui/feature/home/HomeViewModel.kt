package com.agiotagemltda.megusta.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class HomeUiState(
    val posts: List<PostWithTags> = emptyList(),
    val tags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedPostIds: Set<Long> = emptySet(),
    val isNavigating: Boolean = false,
//    var selectAll: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeScreenEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastEditClick = 0L
    private val MIN_EDIT_INTERVAL = 300L


    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.allPostsFlow,
                repository.getAllTagsFlow  // USE AQUI!
            ) { posts, tags ->
                _uiState.update {
                    it.copy(
                        posts = posts,
                        tags = tags,
                        isLoading = false
                    )
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect()
        }
    }

    fun filterByTag(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTag = tag, isLoading = true) }
            if (tag == "Todos") {
                observeData()
                return@launch
            }
            repository.getPostByTag(tag)
                .collect { posts ->
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                }
        }
    }

    fun onEditPostClicked(postId: Long){
        val now = System.currentTimeMillis()
        if (now - lastEditClick < MIN_EDIT_INTERVAL) return
        lastEditClick = now
        viewModelScope.launch {
            _uiState.update{ it.copy(isNavigating = true, isLoading = true)}
            repository.getPostById(postId).firstOrNull()?.let {postWithTags ->
                _events.send(HomeScreenEvent.NavigateToEdit(postId, postWithTags))
            }
            _uiState.update { it.copy(isNavigating = false, isLoading = false) }
        }
    }

    fun exitIsNavigation(){
        _uiState.update { it.copy(isNavigating = false) }
    }

    fun enterSelectionMode(){
        _uiState.update { it.copy(isSelectionMode = true) }
    }

    fun exitSelectionMode(){
        _uiState.update { it.copy(isSelectionMode = false, selectedPostIds = emptySet()) }
    }

    fun togglePostSelection(postId: Long){
        _uiState.update { state ->
            val selected = state.selectedPostIds
            val newSelected = if (postId in selected){
                selected - postId
            } else{
                selected + postId
            }
            state.copy(selectedPostIds = newSelected)
        }
    }

    fun selectAllPosts(){
        _uiState.update { state ->
            val allIds = state.posts.map { it.post.id }.toSet()
            state.copy(selectedPostIds = allIds)
//            state.copy(selectAll = true)
        }
    }

    fun deleteSelectedPosts(){
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedPostIds.toList()
            if (selectedIds.isEmpty()) {
                exitSelectionMode()
//                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            selectedIds.forEach { postId ->
                repository.deletePostTags(postId)
                repository.deletePost(postId)
            }

            exitSelectionMode()
            observeData()
        }
    }
}
