package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas JVM de la lógica de [NoteRepository] usando [FakeNoteDao].
 * No tocan Android ni SQLite: se ejecutan rápido con `./gradlew test`.
 */
class NoteRepositoryTest {

    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        repository = NoteRepository(FakeNoteDao())
    }

    @Test
    fun `save nueva nota la inserta y devuelve un id valido`() = runTest {
        val id = repository.save(Note(title = "Título", text = "Contenido"))

        assertTrue("el id generado debe ser positivo", id > 0)
        assertEquals(1, repository.notes.first().size)
    }

    @Test
    fun `save sobre una nota existente la actualiza sin duplicar`() = runTest {
        val id = repository.save(Note(title = "Original", text = "v1"))

        repository.save(Note(id = id, title = "Editada", text = "v2"))

        val notes = repository.notes.first()
        assertEquals("no debe crear una segunda fila", 1, notes.size)
        assertEquals("Editada", notes.first().title)
        assertEquals("v2", notes.first().text)
    }

    @Test
    fun `save actualiza la marca de tiempo`() = runTest {
        val original = Note(title = "t", text = "c", updatedAt = 0L)

        val id = repository.save(original)

        val stored = repository.getById(id)
        assertNotNull(stored)
        assertTrue("updatedAt debe refrescarse al guardar", stored!!.updatedAt > 0L)
    }

    @Test
    fun `delete elimina la nota`() = runTest {
        val id = repository.save(Note(title = "borrar", text = "x"))

        repository.delete(Note(id = id, title = "borrar", text = "x"))

        assertTrue(repository.notes.first().isEmpty())
        assertNull(repository.getById(id))
    }

    @Test
    fun `search filtra por titulo o contenido`() = runTest {
        repository.save(Note(title = "Lista de compras", text = "leche, pan"))
        repository.save(Note(title = "Ideas", text = "comprar regalo"))
        repository.save(Note(title = "Trabajo", text = "reunión"))

        val porTitulo = repository.search("compras").first()
        assertEquals(1, porTitulo.size)
        assertEquals("Lista de compras", porTitulo.first().title)

        // "compr" aparece en el título de una y en el texto de otra.
        val porTexto = repository.search("compr").first()
        assertEquals(2, porTexto.size)
    }
}
