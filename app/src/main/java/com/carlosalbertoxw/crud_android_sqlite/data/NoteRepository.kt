package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio: capa intermedia entre la UI (ViewModel) y la fuente de datos
 * (el DAO). Aísla al resto de la app de los detalles de Room; si mañana se
 * cambiara la persistencia, solo se tocaría esta clase.
 *
 * @param clock fuente de la hora actual; las pruebas pasan una fija.
 */
class NoteRepository(
    private val dao: NoteDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    val notes: Flow<List<NotePreview>> = dao.observeAll()

    fun search(query: String): Flow<List<NotePreview>> =
        dao.search(query.trim().normalizeForSearch().escapeLike())

    suspend fun getById(id: Long): Note? = dao.getById(id)

    /**
     * Inserta la nota si es nueva (id == 0) o la actualiza si ya existe,
     * refrescando la marca de tiempo y la clave de búsqueda. Devuelve el id
     * resultante.
     */
    suspend fun save(note: Note): Long {
        val prepared =
            note.copy(
                updatedAt = clock(),
                searchKey = searchKeyOf(note.title, note.text)
            )
        return if (note.id == 0L) {
            dao.insert(prepared)
        } else {
            dao.update(prepared)
            note.id
        }
    }

    suspend fun delete(note: Note) = dao.delete(note)

    /**
     * Vuelve a insertar una nota recién borrada tal como estaba (mismo id y
     * fecha), para la acción "Deshacer".
     */
    suspend fun restore(note: Note) {
        dao.insert(note)
    }
}
