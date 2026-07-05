package com.carlosalbertoxw.crud_android_sqlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de notas. Mantiene el estado de la UI (texto de
 * búsqueda y lista visible) y expone acciones para crear, editar y borrar.
 * Sobrevive a los cambios de configuración (p. ej. rotar la pantalla).
 */
class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

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

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    suspend fun loadNote(id: Long): Note? = repository.getById(id)

    fun save(id: Long, title: String, text: String) {
        viewModelScope.launch {
            repository.save(Note(id = id, title = title.trim(), text = text.trim()))
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}

/**
 * Fábrica que sabe construir [NotesViewModel] pasándole el repositorio.
 * Necesaria porque el ViewModel tiene un parámetro en el constructor.
 */
class NotesViewModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            "ViewModel desconocido: ${modelClass.name}"
        }
        return NotesViewModel(repository) as T
    }
}
