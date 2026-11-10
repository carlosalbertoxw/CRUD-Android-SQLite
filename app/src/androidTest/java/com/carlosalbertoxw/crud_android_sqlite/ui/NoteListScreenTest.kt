package com.carlosalbertoxw.crud_android_sqlite.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carlosalbertoxw.crud_android_sqlite.R
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteDatabase
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteListScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.theme.CrudTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI (Compose) de la pantalla de lista. Usan el ViewModel real
 * respaldado por una base Room en memoria.
 */
@RunWith(AndroidJUnit4::class)
class NoteListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun str(id: Int) = composeRule.activity.getString(id)

    private lateinit var db: NoteDatabase
    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NoteListViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NoteRepository(db.noteDao())
        viewModel = NoteListViewModel(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sinNotas_muestraElMensajeVacio() {
        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = {}, onNoteClick = {}) }
        }

        composeRule.onNodeWithText(str(R.string.empty_notes))
            .assertIsDisplayed()
    }

    @Test
    fun botonAgregar_invocaElCallback() {
        var clicked = false
        composeRule.setContent {
            CrudTheme {
                NoteListScreen(viewModel, onAddNote = { clicked = true }, onNoteClick = {})
            }
        }

        composeRule.onNodeWithContentDescription(str(R.string.add_note)).performClick()

        assertTrue(clicked)
    }

    @Test
    fun notaExistente_seMuestraEnLaLista() {
        runBlocking { repository.save(Note(title = "Mi primera nota", text = "hola")) }

        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = {}, onNoteClick = {}) }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Mi primera nota", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Mi primera nota", substring = true).assertIsDisplayed()
    }

    @Test
    fun busqueda_filtraLasNotas() {
        runBlocking {
            repository.save(Note(title = "Compras", text = "leche"))
            repository.save(Note(title = "Trabajo", text = "reunión"))
        }

        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = {}, onNoteClick = {}) }
        }

        // Espera a que ambas notas estén visibles antes de filtrar.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Trabajo", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // En minúsculas a propósito: la búsqueda no distingue mayúsculas.
        composeRule.onNode(hasSetTextAction()).performTextInput("compras")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Trabajo", substring = true)
                .fetchSemanticsNodes().isEmpty()
        }
        // Se excluye el propio buscador con !hasSetTextAction() para
        // comprobar solo la tarjeta de la nota.
        composeRule.onNode(hasText("Compras") and !hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun deslizarNota_laBorraYPermiteDeshacer() {
        runBlocking { repository.save(Note(title = "Deslizable", text = "x")) }

        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = {}, onNoteClick = {}) }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Deslizable").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("Deslizable").performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(str(R.string.undo)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(str(R.string.undo)).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Deslizable").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Deslizable").assertIsDisplayed()
    }
}
