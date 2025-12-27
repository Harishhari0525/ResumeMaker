package com.example.resumemaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.resumemaker.data.ai.AIManager
import com.example.resumemaker.data.pdf.PdfParser
import com.example.resumemaker.ui.EditResumeScreen
import com.example.resumemaker.ui.HistoryScreen
import com.example.resumemaker.ui.MainScreen
import com.example.resumemaker.ui.PdfPreviewScreen
import com.example.resumemaker.ui.ResumeViewModel
import com.example.resumemaker.ui.ResumeViewModelFactory
import com.example.resumemaker.ui.theme.ResumeMakerTheme
import kotlinx.serialization.Serializable

// --- 2025 Type-Safe Routes ---
@Serializable
object HomeRoute

@Serializable
data class PdfPreviewRoute(val htmlCode: String)

@Serializable
object EditResumeRoute // <--- New Route

@Serializable
object HistoryRoute    // <--- New Route

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parser = PdfParser(applicationContext)
        val ai = AIManager()

        val factory = ResumeViewModelFactory(applicationContext, parser, ai)
        val viewModel = ViewModelProvider(this, factory)[ResumeViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            ResumeMakerTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. HOME
                        composable<HomeRoute> {
                            MainScreen(viewModel = viewModel, navController = navController)
                        }

                        // 2. PDF PREVIEW
                        composable<PdfPreviewRoute> { backStackEntry ->
                            val route: PdfPreviewRoute = backStackEntry.toRoute()
                            PdfPreviewScreen(htmlContent = route.htmlCode)
                        }

                        // 3. EDIT RESUME (New)
                        composable<EditResumeRoute> {
                            EditResumeScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 4. HISTORY (New)
                        composable<HistoryRoute> {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}