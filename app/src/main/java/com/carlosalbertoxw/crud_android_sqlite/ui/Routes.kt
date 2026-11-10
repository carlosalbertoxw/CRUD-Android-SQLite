package com.carlosalbertoxw.crud_android_sqlite.ui

import kotlinx.serialization.Serializable

// Rutas de navegación con tipos seguros: el compilador comprueba los
// argumentos en lugar de construir cadenas como "form/{noteId}" a mano.

/** Pantalla de lista de notas. */
@Serializable
data object NoteList

/** Formulario de una nota; [NEW_NOTE_ID] indica que es nueva. */
@Serializable
data class NoteForm(val noteId: Long = NEW_NOTE_ID)
