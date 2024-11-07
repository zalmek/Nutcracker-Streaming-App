package com.example.nutcracker_streaming_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutcracker_streaming_app.DemoScreen.DemoContact
import com.example.nutcracker_streaming_app.DemoScreen.DemoScreen
import com.example.nutcracker_streaming_app.DemoScreen.DemoViewModel
import com.example.nutcracker_streaming_app.Utils.Routes

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) {
                MyAppNavHost()
            }
        }
    }
}

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: DemoViewModel = viewModel()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.MainScreen
    ) {
        composable<Routes.MainScreen> {
            DemoScreen(
                viewModel.viewState.value as DemoContact.State.Main,
                effectFlow = viewModel.effect,
                setEvent = viewModel::setEvent
            )
        }
    }
}
