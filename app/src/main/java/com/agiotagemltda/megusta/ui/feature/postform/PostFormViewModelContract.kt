package com.agiotagemltda.megusta.ui.feature.postform

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface PostFormViewModelContract {
    val uiState: StateFlow<PostFormUiState>
    fun updateName(name: String)
    fun updateTags(tags: String)
    fun updateUrl(url: String)
    fun updateImage(image: String)
    fun updateImageUri(uri: Uri?)
    fun updateNotes(notes: String)
    fun updateRating(newRating: Int)
    fun savePost()
    fun togglePreviewMode()
    fun savePostTogglePreviewMode()
    fun onNewTagContentChange(newValue: String)
    fun toggleTagSelection(tag: String)
    fun addNewTag()
}