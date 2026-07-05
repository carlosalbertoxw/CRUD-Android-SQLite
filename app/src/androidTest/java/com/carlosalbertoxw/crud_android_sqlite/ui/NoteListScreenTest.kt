package com.carlosalbertoxw.crud_android_sqlite.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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

    private lateinit var db: NoteDatabase
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = NotesViewModel(NoteRepository(db.noteDao()))
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

        composeRule.onNodeWithText("Aún no tienes notas", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun botonAgregar_invocaElCallback() {
        var clicked = false
        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = { clicked = true }, onNoteClick = {}) }
        }

        composeRule.onNodeWithContentDescription("Agregar nota").performClick()

        assertTrue(clicked)
    }

    @Test
    fun notaExistente_seMuestraEnLaLista() {
        runBlocking { db.noteDao().insert(Note(title = "Mi primera nota", text = "hola")) }

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
            db.noteDao().insert(Note(title = "Compras", text = "leche"))
            db.noteDao().insert(Note(title = "Trabajo", text = "reunión"))
        }

        composeRule.setContent {
            CrudTheme { NoteListScreen(viewModel, onAddNote = {}, onNoteClick = {}) }
        }

        // Espera a que ambas notas estén visibles antes de filtrar.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Trabajo", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Compras")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Trabajo", substring = true)
                .fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Compras", substring = true).assertIsDisplayed()
    }
}
