package com.carlosalbertoxw.crud_android_sqlite.ui

import com.carlosalbertoxw.crud_android_sqlite.data.FakeNoteDao
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Pruebas JVM de [NoteListViewModel]: búsqueda, borrado y deshacer. */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeNoteDao
    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NoteListViewModel

    @Before
    fun setUp() {
        dao = FakeNoteDao()
        repository = NoteRepository(dao)
        viewModel = NoteListViewModel(repository)
    }

    @Test
    fun `notes muestra todas las notas y filtra al buscar`() = runTest {
        repository.save(Note(title = "Compras", text = "leche"))
        repository.save(Note(title = "Trabajo", text = "reunión"))
        // stateIn(WhileSubscribed) solo emite mientras alguien observa.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }

        assertEquals(2, viewModel.notes.value.size)

        viewModel.onSearchChange("compras")
        // La búsqueda espera una pausa al escribir antes de consultar.
        advanceTimeBy(SEARCH_DEBOUNCE_MS - 1)
        assertEquals(2, viewModel.notes.value.size)
        advanceTimeBy(2)
        assertEquals(listOf("Compras"), viewModel.notes.value.map { it.title })

        // Borrar la búsqueda vuelve a mostrar todo sin esperar.
        viewModel.onSearchChange("")
        advanceTimeBy(1)
        assertEquals(2, viewModel.notes.value.size)
    }

    @Test
    fun `delete borra la nota y la emite para poder deshacer`() = runTest {
        val id = repository.save(Note(title = "Borrar", text = "x"))
        val note = repository.getById(id)!!

        viewModel.delete(id)

        assertNull(repository.getById(id))
        assertEquals(ListEvent.Deleted(note), viewModel.events.first())
    }

    @Test
    fun `undoDelete restaura la nota borrada`() = runTest {
        val id = repository.save(Note(title = "Recuperar", text = "x"))
        val note = repository.getById(id)!!
        viewModel.delete(id)

        val deleted = viewModel.events.first() as ListEvent.Deleted
        viewModel.undoDelete(deleted.note)

        assertEquals(note, repository.getById(id))
        assertTrue(repository.notes.first().isNotEmpty())
    }

    @Test
    fun `si falla el borrado emite Failed y la nota sigue`() = runTest {
        val id = repository.save(Note(title = "Fija", text = "x"))
        dao.failWrites = true

        viewModel.delete(id)

        assertEquals(ListEvent.Failed, viewModel.events.first())
        assertEquals("Fija", repository.getById(id)?.title)
    }
}
