package com.agiotagemltda.megusta.ui.feature.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.repository.PostRepository
import com.agiotagemltda.megusta.domain.model.PostOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
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
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository,
    @ApplicationContext private val context: android.content.Context // Adicione isso
) : ViewModel() {
    // Estados privados para controlar a lógica
    private val _order = MutableStateFlow(PostOrder.ID_DESC)
    private val _selectedTag = MutableStateFlow("Todos")

    // Usamos um Job para poder cancelar a observação antiga e começar a nova
    private var observeJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeScreenEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lastEditClick = 0L
    private val MIN_EDIT_INTERVAL = 300L

    // 1. Novo estado para a busca
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeData()
    }
    // Altera a ordem e dispara a atualização automática
    fun setOrder(newOrder: PostOrder) {
        _order.value = newOrder
    }

    // Altera a tag e dispara a atualização automática
    fun filterByTag(tag: String) {
        _selectedTag.value = tag
    }

    // 2. Função para atualizar a busca (será chamada pela UI)
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
        // Criamos um fluxo para a busca com debounce
            val debouncedSearch = _searchQuery
                .debounce(300L) // Espera 300ms de silêncio após a última alteração
                .distinctUntilChanged() // Só dispara se o texto for realmente diferente do anterior

            // Agora combinamos Ordem, Tag e o fluxo com DEBOUNCE
            combine(_order, _selectedTag, debouncedSearch) { order, tag, query ->
                Triple(order, tag, query)
            }.flatMapLatest { (order, tag, query) ->
                _uiState.update { it.copy(isLoading = true, selectedTag = tag) }

                val postsFlow = repository.getPosts(order)

                combine(postsFlow, repository.getAllTagsFlow) { posts, allTags ->
                    // LÓGICA DE FILTRAGEM TRIPLA:
                    val filtered = posts.filter { pwt ->
                        // 1. Filtro de Tag
                        val matchesTag = tag == "Todos" || pwt.tag.any { it.name == tag }

                        // 2. Filtro de Busca (Nome ou Tags)
                        val matchesQuery = query.isBlank() ||
                                pwt.post.name.contains(query, ignoreCase = true) ||
                                pwt.tag.any { it.name.contains(query, ignoreCase = true) }

                        matchesTag && matchesQuery
                    }
                    filtered to allTags
                }
            }.collect { (posts, allTags) ->
                _uiState.update { it.copy(posts = posts, tags = allTags, isLoading = false) }
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
        }
    }

    fun unselectAllPost(){
        _uiState.update { state ->
            state.copy(selectedPostIds = emptySet())
        }
    }

    fun deleteSelectedPosts(){
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedPostIds.toList()
            if (selectedIds.isEmpty()) {
                exitSelectionMode()
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

    fun writeExportFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = repository.exportAllPostsToJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
        }
    }

    fun readImportFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                repository.importPostsFromJson(json)
            }
        }
    }
}
