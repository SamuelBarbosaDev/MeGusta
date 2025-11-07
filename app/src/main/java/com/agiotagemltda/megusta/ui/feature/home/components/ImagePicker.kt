package com.agiotagemltda.megusta.ui.feature.home.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberImagePicker(
    onImagePicker: (Uri?) -> Unit
): () -> Unit{
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImagePicker(uri)
    }
    return remember {
        {launcher.launch("image/*")}
    }
}

