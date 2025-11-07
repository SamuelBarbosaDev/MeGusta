package com.agiotagemltda.megusta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.agiotagemltda.megusta.navigation.AppNavGraph
import com.agiotagemltda.megusta.ui.theme.MeGustaTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeGustaTheme {
                MeGustaApp()
            }
        }
    }
}

@Composable
fun MeGustaApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    AppNavGraph(navController)
}