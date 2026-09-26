package com.carlosalbertoxw.crud_android_sqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.carlosalbertoxw.crud_android_sqlite.ui.NEW_NOTE_ID
import com.carlosalbertoxw.crud_android_sqlite.ui.NoteFormViewModel
import com.carlosalbertoxw.crud_android_sqlite.ui.NoteListViewModel
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteFormScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteListScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.theme.CrudTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Debe llamarse antes de super.onCreate: muestra el splash con el icono
        // y luego cambia al tema normal de la app.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CrudTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesApp()
                }
            }
        }
    }
}

// Rutas de navegación con tipos seguros: el compilador comprueba los
// argumentos en lugar de construir cadenas como "form/{noteId}" a mano.
@Serializable
private data object NoteList

@Serializable
private data class NoteForm(val noteId: Long = NEW_NOTE_ID)

@Composable
private fun NotesApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NoteList) {
        composable<NoteList> { backStackEntry ->
            NoteListScreen(
                viewModel = viewModel(factory = NoteListViewModel.Factory),
                // dropUnlessResumed ignora toques repetidos mientras ya se está
                // navegando (evita abrir la misma pantalla dos veces).
                onAddNote = dropUnlessResumed { navController.navigate(NoteForm()) },
                onNoteClick = { id ->
                    if (backStackEntry.isResumed()) navController.navigate(NoteForm(id))
                }
            )
        }
        composable<NoteForm> { backStackEntry ->
            val noteId = backStackEntry.toRoute<NoteForm>().noteId
            // El borrado lo hace el ViewModel de la lista (que sigue vivo debajo)
            // para poder mostrar allí el Snackbar de "Deshacer".
            val listEntry = remember(backStackEntry) { navController.getBackStackEntry<NoteList>() }
            val listViewModel: NoteListViewModel =
                viewModel(listEntry, factory = NoteListViewModel.Factory)
            val goBack = dropUnlessResumed { navController.popBackStack() }

            NoteFormScreen(
                viewModel = viewModel(factory = NoteFormViewModel.factory(noteId)),
                onDone = goBack,
                onDelete = {
                    listViewModel.deleteById(noteId)
                    goBack()
                }
            )
        }
    }
}

private fun NavBackStackEntry.isResumed() =
    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
