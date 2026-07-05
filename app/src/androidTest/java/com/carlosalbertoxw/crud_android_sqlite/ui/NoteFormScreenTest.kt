package com.carlosalbertoxw.crud_android_sqlite.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carlosalbertoxw.crud_android_sqlite.data.NoteDatabase
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteFormScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.theme.CrudTheme
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI (Compose) de la pantalla de formulario: validación de campos
 * vacíos y guardado correcto.
 */
@RunWith(AndroidJUnit4::class)
class NoteFormScreenTest {

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
    fun guardarConCamposVacios_muestraValidacion() {
        composeRule.setContent {
            CrudTheme { NoteFormScreen(viewModel, noteId = 0L, onDone = {}) }
        }

        composeRule.onNodeWithContentDescription("Guardar").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Escribe un título", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Escribe un título", substring = true).assertIsDisplayed()
    }

    @Test
    fun guardarConDatosValidos_invocaOnDone() {
        var done = false
        composeRule.setContent {
            CrudTheme { NoteFormScreen(viewModel, noteId = 0L, onDone = { done = true }) }
        }

        val campos = composeRule.onAllNodes(hasSetTextAction())
        campos[0].performTextInput("Título de prueba")
        campos[1].performTextInput("Contenido de prueba")

        composeRule.onNodeWithContentDescription("Guardar").performClick()

        assertTrue(done)
    }
}
