package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación en memoria de [NoteDao] para pruebas JVM: replica el
 * comportamiento observable de Room (ordena por fecha desc., recorta la vista
 * previa, busca en `searchKey` distinguiendo mayúsculas igual que `LIKE` sobre
 * texto ya normalizado) sin necesitar Android ni SQLite reales.
 */
class FakeNoteDao : NoteDao {
    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    /** Si es true, las escrituras fallan como lo haría una base dañada o llena. */
    var failWrites = false

    override fun observeAll(): Flow<List<NotePreview>> = notes.map { list -> list.toPreviews() }

    override fun search(pattern: String): Flow<List<NotePreview>> {
        // El patrón llega escapado para `LIKE ... ESCAPE '\'`: se quitan las
        // barras para comparar con el texto literal.
        val query = pattern.replace(Regex("""\\(.)"""), "$1")
        return notes.map { list -> list.filter { query in it.searchKey }.toPreviews() }
    }

    override suspend fun getById(id: Long): Note? = notes.value.firstOrNull { it.id == id }

    override suspend fun insert(note: Note): Long {
        checkWritable()
        // Como Room: si la nota ya trae id (p. ej. al deshacer un borrado) se respeta.
        val id = if (note.id != 0L) note.id else nextId++
        nextId = maxOf(nextId, id + 1)
        notes.value = notes.value + note.copy(id = id)
        return id
    }

    override suspend fun update(note: Note) {
        checkWritable()
        notes.value = notes.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun delete(note: Note) {
        checkWritable()
        notes.value = notes.value.filterNot { it.id == note.id }
    }

    private fun checkWritable() = check(!failWrites) { "Escritura fallida (simulada)" }

    private fun List<Note>.toPreviews() = sortedByDescending { it.updatedAt }
        .map { NotePreview(it.id, it.title, it.text.take(PREVIEW_LENGTH), it.updatedAt) }
}
