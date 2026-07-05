package com.carlosalbertoxw.crud_android_sqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carlosalbertoxw.crud_android_sqlite.ui.NotesViewModel
import com.carlosalbertoxw.crud_android_sqlite.ui.NotesViewModelFactory
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteFormScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteListScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.theme.CrudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repositorio creado en la Application (una sola instancia para toda la app).
        val repository = (application as CrudApplication).repository
        val factory = NotesViewModelFactory(repository)

        setContent {
            CrudTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesApp(factory)
                }
            }
        }
    }
}

private object Routes {
    const val LIST = "list"
    const val FORM = "form"
    const val ARG_ID = "noteId"
    fun form(noteId: Long = 0L) = "$FORM/$noteId"
}

@Composable
private fun NotesApp(factory: NotesViewModelFactory) {
    val navController = rememberNavController()
    // Un único ViewModel compartido por ambas pantallas.
    val viewModel: NotesViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            NoteListScreen(
                viewModel = viewModel,
                onAddNote = { navController.navigate(Routes.form()) },
                onNoteClick = { id -> navController.navigate(Routes.form(id)) }
            )
        }
        composable(
            route = "${Routes.FORM}/{${Routes.ARG_ID}}",
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong(Routes.ARG_ID) ?: 0L
            NoteFormScreen(
                viewModel = viewModel,
                noteId = noteId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
