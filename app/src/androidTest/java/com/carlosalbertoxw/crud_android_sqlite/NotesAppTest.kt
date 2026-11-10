package com.carlosalbertoxw.crud_android_sqlite

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de extremo a extremo: arranca la app real (MainActivity, navegación,
 * base de datos del dispositivo) y recorre crear, abrir, borrar y deshacer.
 * Al terminar borra la nota que creó.
 */
@RunWith(AndroidJUnit4::class)
class NotesAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun str(id: Int) = composeRule.activity.getString(id)

    // Título único para no confundirse con notas que ya existan.
    private val title = "E2E ${System.nanoTime()}"

    // La tarjeta de la nota en la lista (no el campo de texto del formulario).
    private val noteCard = hasText(title) and !hasSetTextAction()

    @Test
    fun crearAbrirBorrarYDeshacer() {
        // Crear desde la lista.
        click(hasContentDescription(str(R.string.add_note)))
        waitFor(hasText(str(R.string.new_note)))
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput(title)
        fields[1].performTextInput("Creada desde la prueba")
        click(hasContentDescription(str(R.string.save)))

        // Al guardar vuelve a la lista, que ya muestra la nota.
        waitFor(noteCard)

        // Abrirla y borrarla desde el formulario.
        click(noteCard)
        waitFor(hasText(str(R.string.edit_note)))
        click(hasContentDescription(str(R.string.delete)) and hasClickAction())

        // La lista ofrece deshacer y la nota vuelve.
        click(hasText(str(R.string.undo)))
        waitFor(noteCard)

        // Limpieza: borrarla de nuevo, ahora sin deshacer.
        click(noteCard)
        waitFor(hasText(str(R.string.edit_note)))
        click(hasContentDescription(str(R.string.delete)) and hasClickAction())
        composeRule.waitUntil(TIMEOUT) {
            composeRule.onAllNodes(noteCard).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitFor(matcher: SemanticsMatcher) {
        composeRule.waitUntil(TIMEOUT) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun click(matcher: SemanticsMatcher) {
        waitFor(matcher)
        composeRule.onAllNodes(matcher).onFirst().performClick()
    }

    private companion object {
        const val TIMEOUT = 5_000L
    }
}
