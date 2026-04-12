package com.agiotagemltda.megusta.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agiotagemltda.megusta.data.local.entity.PostWithTags
import com.agiotagemltda.megusta.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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
//    var selectAll: Boolean = false
)

enum class PostOrder {
    ID_DESC, ID_ASC, NAME_ASC, NAME_DESC
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
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

                val postsFlow = when (order) {
                    PostOrder.ID_DESC -> repository.allPostsFlow
                    PostOrder.ID_ASC -> repository.allASCPostsFlow
                    PostOrder.NAME_ASC -> repository.allABCPostsFlow
                    PostOrder.NAME_DESC -> repository.allDescABCPostsFlow
                }

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
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    private fun observeData() {
//        viewModelScope.launch {
//            // Combinamos a ordem e a tag. Se qualquer um mudar, o bloco abaixo roda.
//            combine(_order, _selectedTag) { order, tag ->
//                order to tag
//            }.flatMapLatest { (order, tag) ->
//                _uiState.update { it.copy(isLoading = true, selectedTag = tag) }
//
//                val postsFlow = when (order) {
//                    PostOrder.ID_DESC -> repository.allPostsFlow
//                    PostOrder.ID_ASC -> repository.allASCPostsFlow
//                    PostOrder.NAME_ASC -> repository.allABCPostsFlow
//                    PostOrder.NAME_DESC -> repository.allDescABCPostsFlow
//                }
//
//                combine(postsFlow, repository.getAllTagsFlow) { posts, allTags ->
//                    val filtered = if (tag == "Todos") posts
//                    else posts.filter { p -> p.tag.any { it.name == tag } }
//                    filtered to allTags
//                }
//            }.collect { (posts, allTags) ->
//                _uiState.update { it.copy(posts = posts, tags = allTags, isLoading = false) }
//            }
//        }
//    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    private fun observeData() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            // A MÁGICA: flatMapLatest observa o fluxo '_order'.
//            // Toda vez que '_order' muda, ele descarta o fluxo antigo e assina o novo.
//            _order.flatMapLatest { selectedOrder ->
//                val postsFlow = when (selectedOrder) {
//                    PostOrder.ID_DESC -> repository.allPostsFlow
//                    PostOrder.ID_ASC -> repository.allASCPostsFlow
//                    PostOrder.NAME_ASC -> repository.allABCPostsFlow
//                    PostOrder.NAME_DESC -> repository.allDescABCPostsFlow
//                }
//
//                // Combinamos o fluxo de posts escolhido com o de tags
//                combine(postsFlow, repository.getAllTagsFlow) { posts, tags ->
//                    posts to tags
//                }
//            }
//                .catch { e ->
//                    _uiState.update { it.copy(error = e.message, isLoading = false) }
//                }
//                .collect { (posts, tags) ->
//                    _uiState.update {
//                        it.copy(posts = posts, tags = tags, isLoading = false)
//                    }
//                }
//        }
//    }

//    fun setOrder(newOrder: PostOrder) {
//        if (_order.value == newOrder) return
//        _order.value = newOrder
//        observeData() // Reinicia a observação com a nova ordem
//    }
//
//    private fun observeData() {
//        observeJob?.cancel() // Cancela a observação da ordem anterior
//        observeJob = viewModelScope.launch {
//            // Escolhe o Flow correto baseado na ordem selecionada
//            val postsFlow = when (_order.value) {
//                PostOrder.ID_DESC -> repository.allPostsFlow
//                PostOrder.ID_ASC -> repository.allASCPostsFlow
//                PostOrder.NAME_ASC -> repository.allABCPostsFlow
//                PostOrder.NAME_DESC -> repository.allDescABCPostsFlow
//            }
//
//            combine(
//                postsFlow,
//                repository.getAllTagsFlow
//            ) { posts, tags ->
//                _uiState.update {
//                    it.copy(posts = posts, tags = tags, isLoading = false)
//                }
//            }.catch { e ->
//                _uiState.update { it.copy(error = e.message, isLoading = false) }
//            }.collect()
//        }
//    }

//    private fun observeData() {
//        viewModelScope.launch {
//            combine(
//                repository.allPostsFlow,
//                repository.getAllTagsFlow  // USE AQUI!
//            ) { posts, tags ->
//                _uiState.update {
//                    it.copy(
//                        posts = posts,
//                        tags = tags,
//                        isLoading = false
//                    )
//                }
//            }
//                .catch { e ->
//                    _uiState.update { it.copy(error = e.message, isLoading = false) }
//                }
//                .collect()
//        }
//    }

//    fun filterByTag(tag: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(selectedTag = tag, isLoading = true) }
//            if (tag == "Todos") {
//                observeData()
//                return@launch
//            }
//            repository.getPostByTag(tag)
//                .collect { posts ->
//                    _uiState.update { it.copy(posts = posts, isLoading = false) }
//                }
//        }
//    }

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

    fun selectAllPostsASC(){
        viewModelScope.launch {
            combine(
                repository.allASCPostsFlow,
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

    fun selectAllPostsABC(){
        viewModelScope.launch {
            combine(
                repository.allASCPostsFlow,
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
}
