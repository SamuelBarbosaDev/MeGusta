package com.agiotagemltda.megusta.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.agiotagemltda.megusta.ui.feature.add.AddPostViewModel
import com.agiotagemltda.megusta.ui.feature.home.HomeScreen
import com.agiotagemltda.megusta.ui.feature.postform.EditPostViewModel
import com.agiotagemltda.megusta.ui.feature.postform.PostFormScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController,
        startDestination = "home",
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(300)) }
    ) {
        composable("home") { HomeScreen(navController) }

        // ADICIONAR
        composable("add_post") {
            val vm: AddPostViewModel = hiltViewModel()
            PostFormScreen(navController, vm, isEditMode = false)
        }

        // EDITAR
        composable(
            route = "edit_post/{postId}?post_data={post_data}",
            arguments = listOf(
                navArgument("postId") { type = NavType.LongType },
                navArgument("post_data") {
                    type = NavType.StringType
                    nullable = true
                }
            ),
            // ANIMAÇÃO DE ENTRADA
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            // ANIMAÇÃO DE SAÍDA
            exitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            // VOLTANDO (pop)
            popEnterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: return@composable
            val postDataJson = backStackEntry.arguments?.getString("post_data")

            val viewModel = hiltViewModel<EditPostViewModel, EditPostViewModel.Factory>(
                key = "edit_$postId"
            ){ factory -> factory.create(postId)}

            LaunchedEffect(postDataJson) {
                postDataJson?.let{
                    viewModel.savedStateHandle["post_data"] = it
                }
            }

            PostFormScreen(navController, viewModel, isEditMode = true)
        }
    }
}