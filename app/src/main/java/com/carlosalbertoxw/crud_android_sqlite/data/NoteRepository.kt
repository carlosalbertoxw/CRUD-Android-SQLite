package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio: capa intermedia entre la UI (ViewModel) y la fuente de datos
 * (el DAO). Aísla al resto de la app de los detalles de Room; si mañana se
 * cambiara la persistencia, solo se tocaría esta clase.
 */
class NoteRepository(private val dao: NoteDao) {

    val notes: Flow<List<Note>> = dao.observeAll()

    fun search(query: String): Flow<List<Note>> = dao.search(query)

    suspend fun getById(id: Long): Note? = dao.getById(id)

    /**
     * Inserta la nota si es nueva (id == 0) o la actualiza si ya existe,
     * refrescando siempre la marca de tiempo. Devuelve el id resultante.
     */
    suspend fun save(note: Note): Long {
        val stamped = note.copy(updatedAt = System.currentTimeMillis())
        return if (note.id == 0L) {
            dao.insert(stamped)
        } else {
            dao.update(stamped)
            note.id
        }
    }

    suspend fun delete(note: Note) = dao.delete(note)
}
