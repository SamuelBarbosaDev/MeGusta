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

    // Métodos da interface
    override fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    override fun updateTags(tags: String) = _uiState.update { it.copy(tagsInput = tags) }
    override fun updateUrl(url: String) = _uiState.update { it.copy(url = url) }
    override fun updateImage(image: String) = _uiState.update { it.copy(image = image) }
    override fun updateImageUri(uri: Uri?) = _uiState.update { it.copy(imageUri = uri) }
    override fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }

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