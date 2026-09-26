package com.carlosalbertoxw.crud_android_sqlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.carlosalbertoxw.crud_android_sqlite.CrudApplication
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de la lista de notas. Mantiene el texto de búsqueda y la lista
 * visible, y se encarga de borrar notas (con opción de deshacer).
 * Sobrevive a los cambios de configuración (p. ej. rotar la pantalla).
 */
class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Lista de notas que se muestra. Cambia sola cuando:
     *  - cambia el texto de búsqueda, o
     *  - cambian los datos en la base (gracias a que Room devuelve Flow).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.notes else repository.search(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Canal y no StateFlow: cada borrado es un evento que se consume una sola
    // vez (un Snackbar), aunque la pantalla no esté visible en ese momento.
    private val _deletedNotes = Channel<Note>(Channel.BUFFERED)

    /** Notas recién borradas; la pantalla ofrece "Deshacer" para cada una. */
    val deletedNotes: Flow<Note> = _deletedNotes.receiveAsFlow()

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
            _deletedNotes.send(note)
        }
    }

    /** Borra por id; lo usa el formulario, que no tiene la nota guardada a mano. */
    fun deleteById(id: Long) {
        viewModelScope.launch {
            val note = repository.getById(id) ?: return@launch
            repository.delete(note)
            _deletedNotes.send(note)
        }
    }

    fun undoDelete(note: Note) {
        viewModelScope.launch {
            repository.restore(note)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { NoteListViewModel(repository()) }
        }
    }
}

/** Repositorio único de la app, disponible al crear cualquier ViewModel. */
internal fun CreationExtras.repository(): NoteRepository =
    (checkNotNull(this[APPLICATION_KEY]) as CrudApplication).repository
