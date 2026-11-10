package com.carlosalbertoxw.crud_android_sqlite.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): aquí se declaran las operaciones sobre la base de
 * datos. Room genera la implementación con el SQL correspondiente.
 *
 * - Las consultas que devuelven [Flow] son "reactivas": cada vez que cambian
 *   los datos de la tabla, el Flow vuelve a emitir la lista actualizada.
 * - Las operaciones de escritura son `suspend` para ejecutarse fuera del hilo
 *   principal (sin bloquear la interfaz).
 */
@Dao
interface NoteDao {
    @Query(
        """
        SELECT id, title, substr(text, 1, $PREVIEW_LENGTH) AS preview, updatedAt
        FROM notes
        ORDER BY updatedAt DESC
        """
    )
    fun observeAll(): Flow<List<NotePreview>>

    /**
     * Busca [pattern] dentro de la columna `searchKey`. El patrón debe llegar
     * normalizado (ver [normalizeForSearch]) y con `%`, `_` y `\` escapados
     * (ver [escapeLike]) para que se busquen de forma literal y no como
     * comodines de `LIKE`.
     */
    @Query(
        """
        SELECT id, title, substr(text, 1, $PREVIEW_LENGTH) AS preview, updatedAt
        FROM notes
        WHERE searchKey LIKE '%' || :pattern || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
        """
    )
    fun search(pattern: String): Flow<List<NotePreview>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}

/** Escapa los comodines de `LIKE` para que el texto se busque tal cual. */
fun String.escapeLike(): String = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
