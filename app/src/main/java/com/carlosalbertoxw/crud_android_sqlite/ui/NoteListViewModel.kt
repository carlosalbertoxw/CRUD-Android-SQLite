package com.carlosalbertoxw.crud_android_sqlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NotePreview
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import com.carlosalbertoxw.crud_android_sqlite.repository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Espera tras la última tecla antes de consultar la base al buscar. */
const val SEARCH_DEBOUNCE_MS = 150L

/** Eventos de una sola vez que la pantalla de lista debe atender. */
sealed interface ListEvent {
    /** Se borró una nota; la pantalla ofrece "Deshacer". */
    data class Deleted(val note: Note) : ListEvent

    /** Falló un borrado o un "Deshacer". */
    data object Failed : ListEvent
}

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
     *  - cambia el texto de búsqueda (tras una breve pausa al escribir), o
     *  - cambian los datos en la base (gracias a que Room devuelve Flow).
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notes: StateFlow<List<NotePreview>> =
        _searchQuery
            .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
            .flatMapLatest { query ->
                if (query.isBlank()) repository.notes else repository.search(query)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // Canal y no StateFlow: cada evento se consume una sola vez (un Snackbar),
    // aunque la pantalla no esté visible en ese momento.
    private val _events = Channel<ListEvent>(Channel.BUFFERED)
    val events: Flow<ListEvent> = _events.receiveAsFlow()

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    /** Borra la nota [id]; se lee completa antes para poder deshacer. */
    fun delete(id: Long) = launchReportingErrors {
        val note = repository.getById(id) ?: return@launchReportingErrors
        repository.delete(note)
        _events.send(ListEvent.Deleted(note))
    }

    fun undoDelete(note: Note) = launchReportingErrors { repository.restore(note) }

    private fun launchReportingErrors(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ListEvent.Failed)
            }
        }
    }

    companion object {
        val Factory =
            viewModelFactory {
                initializer { NoteListViewModel(repository()) }
            }
    }
}
