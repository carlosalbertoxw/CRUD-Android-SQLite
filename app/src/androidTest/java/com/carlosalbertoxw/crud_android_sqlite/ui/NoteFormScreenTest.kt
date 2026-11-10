package com.carlosalbertoxw.crud_android_sqlite.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carlosalbertoxw.crud_android_sqlite.R
import com.carlosalbertoxw.crud_android_sqlite.data.NoteDatabase
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import com.carlosalbertoxw.crud_android_sqlite.ui.screens.NoteFormScreen
import com.carlosalbertoxw.crud_android_sqlite.ui.theme.CrudTheme
import org.junit.After
import org.junit.Assert.assertFalse
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

    private fun str(id: Int) = composeRule.activity.getString(id)

    private lateinit var db: NoteDatabase
    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NoteRepository(db.noteDao())
    }

    private fun newNoteViewModel() = NoteFormViewModel(NEW_NOTE_ID, SavedStateHandle(), repository)

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun guardarConCamposVacios_muestraValidacion() {
        val viewModel = newNoteViewModel()
        composeRule.setContent {
            CrudTheme { NoteFormScreen(viewModel, onDone = {}) }
        }

        composeRule.onNodeWithContentDescription(str(R.string.save)).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(str(R.string.validation_empty))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(str(R.string.validation_empty)).assertIsDisplayed()
    }

    @Test
    fun guardarConDatosValidos_invocaOnDone() {
        var done = false
        val viewModel = newNoteViewModel()
        composeRule.setContent {
            CrudTheme { NoteFormScreen(viewModel, onDone = { done = true }) }
        }

        val campos = composeRule.onAllNodes(hasSetTextAction())
        campos[0].performTextInput("Título de prueba")
        campos[1].performTextInput("Contenido de prueba")

        composeRule.onNodeWithContentDescription(str(R.string.save)).performClick()

        // El guardado es asíncrono (Room escribe fuera del hilo principal).
        composeRule.waitUntil(timeoutMillis = 5_000) { done }
    }

    @Test
    fun volverConCambiosSinGuardar_pideConfirmacion() {
        var done = false
        val viewModel = newNoteViewModel()
        composeRule.setContent {
            CrudTheme { NoteFormScreen(viewModel, onDone = { done = true }) }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Borrador")
        composeRule.onNodeWithContentDescription(str(R.string.back)).performClick()

        composeRule.onNodeWithText(str(R.string.discard_title)).assertIsDisplayed()
        assertFalse(done)

        composeRule.onNodeWithText(str(R.string.discard)).performClick()
        assertTrue(done)
    }
}
