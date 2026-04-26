package com.agiotagemltda.megusta.ui.feature.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController


@Composable
fun AddPostScreen(
    navController: NavController,
    viewModel: AddPostViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
//    var name by remember { mutableStateOf("") }
//    var tags by remember { mutableStateOf("") }
//    var url by remember { mutableStateOf("") }
//    var notes by remember { mutableStateOf("") }
//    var image by remember { mutableStateOf("") }
//
//    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    viewModel.savePost(
//                        name= name,
//                        tagsInput = tags,
//                        url = url,
//                        notes = notes,
//                        image = image
//                    )
//                    navController.popBackStack()
//                }
//            ) {
//                Icon(Icons.Default.Done, null)
//            }
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = modifier
//                .padding(innerPadding)
//                .padding(20.dp)
//                .fillMaxSize()
//        ) {
//            OutlinedTextField(
//                value = name,
//                onValueChange = { name = it },
//                label = { Text("Nome") },
//                modifier = modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = tags,
//                onValueChange = { tags = it },
//                label = { Text("Tags") },
//                placeholder = { Text("ex: Ação, Anime, Ficção") },
//                modifier = modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = url,
//                onValueChange = { url = it },
//                label = { Text("URL") },
//                modifier = modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = image,
//                onValueChange = { image = it },
//                label = { Text("Imagem") },
//                modifier = modifier.fillMaxWidth()
//            )
//            OutlinedTextField(
//                value = notes,
//                onValueChange = { notes = it },
//                label = { Text("Anotações") },
//                modifier = modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight()
//            )
//        }
//    }
}