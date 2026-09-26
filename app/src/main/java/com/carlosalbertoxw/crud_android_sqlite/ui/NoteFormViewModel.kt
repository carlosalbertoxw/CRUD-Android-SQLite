package com.carlosalbertoxw.crud_android_sqlite.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Id que indica "nota nueva" (Room genera el id real al insertar). */
const val NEW_NOTE_ID = 0L

/** Eventos de una sola vez que la pantalla del formulario debe atender. */
sealed interface FormEvent {
    /** La nota se guardó: volver a la lista. */
    data object Saved : FormEvent

    /** La nota a editar ya no existe (p. ej. se borró): volver a la lista. */
    data object NotFound : FormEvent

    /** Falta el título o el contenido. */
    data object ValidationError : FormEvent
}

/**
 * ViewModel del formulario de crear/editar una nota.
 *
 * El borrador (título y texto) vive en [SavedStateHandle]: así sobrevive a la
 * rotación de pantalla e incluso a que Android cierre el proceso en segundo
 * plano, y la nota se lee de la base una sola vez.
 */
class NoteFormViewModel(
    private val noteId: Long,
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository
) : ViewModel() {

    val isEditing = noteId != NEW_NOTE_ID

    val title: StateFlow<String> = savedStateHandle.getStateFlow(KEY_TITLE, "")
    val text: StateFlow<String> = savedStateHandle.getStateFlow(KEY_TEXT, "")

    /** true si el borrador difiere de lo guardado (o de vacío, si es nueva). */
    val hasChanges: StateFlow<Boolean> = combine(
        title,
        text,
        savedStateHandle.getStateFlow(KEY_ORIGINAL_TITLE, ""),
        savedStateHandle.getStateFlow(KEY_ORIGINAL_TEXT, "")
    ) { title, text, originalTitle, originalText ->
        title != originalTitle || text != originalText
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _events = Channel<FormEvent>(Channel.BUFFERED)
    val events: Flow<FormEvent> = _events.receiveAsFlow()

    // Evita guardar dos veces si se pulsa el botón repetidamente.
    private var isSaving = false

    init {
        if (isEditing && savedStateHandle.get<Boolean>(KEY_LOADED) != true) {
            viewModelScope.launch { load() }
        }
    }

    private suspend fun load() {
        val note = repository.getById(noteId)
        if (note == null) {
            _events.send(FormEvent.NotFound)
            return
        }
        savedStateHandle[KEY_ORIGINAL_TITLE] = note.title
        savedStateHandle[KEY_ORIGINAL_TEXT] = note.text
        savedStateHandle[KEY_TITLE] = note.title
        savedStateHandle[KEY_TEXT] = note.text
        savedStateHandle[KEY_LOADED] = true
    }

    fun onTitleChange(value: String) {
        savedStateHandle[KEY_TITLE] = value
    }

    fun onTextChange(value: String) {
        savedStateHandle[KEY_TEXT] = value
    }

    fun save() {
        if (isSaving) return
        val title = title.value.trim()
        val text = text.value.trim()
        if (title.isEmpty() || text.isEmpty()) {
            _events.trySend(FormEvent.ValidationError)
            return
        }
        isSaving = true
        viewModelScope.launch {
            repository.save(Note(id = noteId, title = title, text = text))
            _events.send(FormEvent.Saved)
        }
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_TEXT = "text"
        private const val KEY_ORIGINAL_TITLE = "originalTitle"
        private const val KEY_ORIGINAL_TEXT = "originalText"
        private const val KEY_LOADED = "loaded"

        fun factory(noteId: Long) = viewModelFactory {
            initializer {
                NoteFormViewModel(noteId, createSavedStateHandle(), repository())
            }
        }
    }
}
