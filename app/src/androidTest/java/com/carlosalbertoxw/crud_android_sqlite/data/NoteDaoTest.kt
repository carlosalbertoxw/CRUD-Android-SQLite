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
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.noteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadById() = runTest {
        val id = dao.insert(Note(title = "Hola", text = "Mundo"))

        val loaded = dao.getById(id)
        assertEquals("Hola", loaded?.title)
        assertEquals("Mundo", loaded?.text)
    }

    @Test
    fun observeAllOrdersByUpdatedAtDesc() = runTest {
        dao.insert(Note(title = "Vieja", text = "x", updatedAt = 1_000L))
        dao.insert(Note(title = "Nueva", text = "y", updatedAt = 2_000L))

        val notes = dao.observeAll().first()
        assertEquals(2, notes.size)
        assertEquals("la más reciente va primero", "Nueva", notes.first().title)
    }

    @Test
    fun updateChangesStoredValues() = runTest {
        val id = dao.insert(Note(title = "Antes", text = "v1"))

        dao.update(Note(id = id, title = "Después", text = "v2"))

        val loaded = dao.getById(id)
        assertEquals("Después", loaded?.title)
        assertEquals("v2", loaded?.text)
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val id = dao.insert(Note(title = "Temporal", text = "z"))

        dao.delete(Note(id = id, title = "Temporal", text = "z"))

        assertNull(dao.getById(id))
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun searchMatchesTitleOrText() = runTest {
        dao.insert(Note(title = "Lista de compras", text = "leche"))
        dao.insert(Note(title = "Ideas", text = "comprar regalo"))
        dao.insert(Note(title = "Trabajo", text = "reunión"))

        val results = dao.search("compr").first()
        assertEquals("debe coincidir en título y en texto", 2, results.size)
    }
}
