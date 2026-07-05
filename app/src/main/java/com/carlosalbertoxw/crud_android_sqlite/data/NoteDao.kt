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

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
           OR text  LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
