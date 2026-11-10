package com.carlosalbertoxw.crud_android_sqlite.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas JVM de la lógica de [NoteRepository] usando [FakeNoteDao].
 * No tocan Android ni SQLite: se ejecutan rápido con `./gradlew test`.
 */
class NoteRepositoryTest {
    private var now = 1_000L
    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        repository = NoteRepository(FakeNoteDao(), clock = { now })
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
        assertEquals("v2", notes.first().preview)
    }

    @Test
    fun `save asigna la hora del reloj`() = runTest {
        val id = repository.save(Note(title = "t", text = "c"))
        assertEquals(1_000L, repository.getById(id)!!.updatedAt)

        now = 5_000L
        repository.save(Note(id = id, title = "t", text = "editada"))

        assertEquals(5_000L, repository.getById(id)!!.updatedAt)
    }

    @Test
    fun `save calcula la clave de busqueda`() = runTest {
        val id = repository.save(Note(title = "Canción", text = "AÑO Nuevo"))

        assertEquals("cancion\nano nuevo", repository.getById(id)!!.searchKey)
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

    @Test
    fun `search ignora mayusculas y acentos`() = runTest {
        repository.save(Note(title = "REUNIÓN", text = "Con el equipo"))

        assertEquals(1, repository.search("reunion").first().size)
        assertEquals(1, repository.search("Reunión").first().size)
        assertEquals(1, repository.search("  EQUIPO ").first().size)
    }

    @Test
    fun `search trata los comodines de LIKE como texto literal`() = runTest {
        repository.save(Note(title = "Descuento", text = "50% en todo"))
        repository.save(Note(title = "Otra", text = "sin porcentaje"))

        assertEquals(1, repository.search("%").first().size)
        assertTrue(repository.search("_").first().isEmpty())
    }

    @Test
    fun `la lista solo trae una vista previa del contenido`() = runTest {
        repository.save(Note(title = "Larga", text = "x".repeat(PREVIEW_LENGTH * 3)))

        assertEquals(PREVIEW_LENGTH, repository.notes.first().single().preview.length)
    }

    @Test
    fun `escapeLike escapa comodines y la barra invertida`() {
        assertEquals("""100\% a\_b c\\d""", """100% a_b c\d""".escapeLike())
    }

    @Test
    fun `normalizeForSearch quita acentos y pasa a minusculas`() {
        assertEquals("cancion ano pinguino", "Canción AÑO Pingüino".normalizeForSearch())
    }

    @Test
    fun `restore vuelve a insertar la nota borrada con el mismo id`() = runTest {
        val id = repository.save(Note(title = "Importante", text = "no borrar"))
        val note = repository.getById(id)!!
        repository.delete(note)

        repository.restore(note)

        assertEquals(note, repository.getById(id))
    }
}
