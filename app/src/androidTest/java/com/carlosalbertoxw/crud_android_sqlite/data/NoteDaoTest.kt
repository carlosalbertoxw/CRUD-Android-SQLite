package com.carlosalbertoxw.crud_android_sqlite.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas del [NoteDao] contra una base Room REAL en memoria (SQLite de
 * Android). Se ejecutan en un emulador o dispositivo con `connectedAndroidTest`.
 * La base es efímera: se crea antes de cada test y se cierra después.
 */
@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var db: NoteDatabase
    private lateinit var dao: NoteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.noteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    /** Nota con su clave de búsqueda calculada, como la guarda el repositorio. */
    private fun note(title: String, text: String, updatedAt: Long = 0) = Note(
        title = title,
        text = text,
        updatedAt = updatedAt,
        searchKey = searchKeyOf(title, text)
    )

    @Test
    fun insertAndReadById() = runTest {
        val id = dao.insert(note("Hola", "Mundo"))

        val loaded = dao.getById(id)
        assertEquals("Hola", loaded?.title)
        assertEquals("Mundo", loaded?.text)
    }

    @Test
    fun observeAllOrdersByUpdatedAtDesc() = runTest {
        dao.insert(note("Vieja", "x", updatedAt = 1_000L))
        dao.insert(note("Nueva", "y", updatedAt = 2_000L))

        val notes = dao.observeAll().first()
        assertEquals(2, notes.size)
        assertEquals("la más reciente va primero", "Nueva", notes.first().title)
    }

    @Test
    fun observeAllTruncatesPreview() = runTest {
        dao.insert(note("Larga", "x".repeat(PREVIEW_LENGTH * 3)))

        assertEquals(PREVIEW_LENGTH, dao.observeAll().first().single().preview.length)
    }

    @Test
    fun updateChangesStoredValues() = runTest {
        val id = dao.insert(note("Antes", "v1"))

        dao.update(note("Después", "v2").copy(id = id))

        val loaded = dao.getById(id)
        assertEquals("Después", loaded?.title)
        assertEquals("v2", loaded?.text)
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val id = dao.insert(note("Temporal", "z"))

        dao.delete(Note(id = id, title = "Temporal", text = "z"))

        assertNull(dao.getById(id))
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun searchMatchesTitleOrText() = runTest {
        dao.insert(note("Lista de compras", "leche"))
        dao.insert(note("Ideas", "comprar regalo"))
        dao.insert(note("Trabajo", "reunión"))

        val results = dao.search("compr").first()
        assertEquals("debe coincidir en título y en texto", 2, results.size)
    }

    @Test
    fun searchIgnoresCaseAndAccents() = runTest {
        dao.insert(note("REUNIÓN", "Año nuevo"))

        assertEquals(1, dao.search("Reunión".normalizeForSearch()).first().size)
        assertEquals(1, dao.search("ano".normalizeForSearch()).first().size)
    }

    @Test
    fun searchTreatsWildcardsLiterally() = runTest {
        dao.insert(note("Descuento", "50% en todo"))
        dao.insert(note("Otra", "sin porcentaje"))

        assertEquals(1, dao.search("%".escapeLike()).first().size)
        assertTrue(dao.search("_".escapeLike()).first().isEmpty())
    }
}
