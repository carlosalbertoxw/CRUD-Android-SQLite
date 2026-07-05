package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [NoteDao] para pruebas JVM: replica el
 * comportamiento observable de Room (ordena por fecha desc., filtra en la
 * búsqueda) sin necesitar Android ni SQLite reales.
 */
class FakeNoteDao : NoteDao {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Note>> =
        notes.map { list -> list.sortedByDescending { it.updatedAt } }

    override fun search(query: String): Flow<List<Note>> =
        notes.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.text.contains(query, ignoreCase = true)
            }.sortedByDescending { it.updatedAt }
        }

    override suspend fun getById(id: Long): Note? =
        notes.value.firstOrNull { it.id == id }

    override suspend fun insert(note: Note): Long {
        val id = nextId++
        notes.value = notes.value + note.copy(id = id)
        return id
    }

    override suspend fun update(note: Note) {
        notes.value = notes.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun delete(note: Note) {
        notes.value = notes.value.filterNot { it.id == note.id }
    }
}
