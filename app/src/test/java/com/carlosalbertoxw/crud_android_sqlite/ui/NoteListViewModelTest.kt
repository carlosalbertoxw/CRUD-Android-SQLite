package com.carlosalbertoxw.crud_android_sqlite.ui

import com.carlosalbertoxw.crud_android_sqlite.data.FakeNoteDao
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private lateinit var repository: NoteRepository
    private lateinit var viewModel: NoteListViewModel

    @Before
    fun setUp() {
        repository = NoteRepository(FakeNoteDao())
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
        assertEquals(listOf("Compras"), viewModel.notes.value.map { it.title })

        viewModel.onSearchChange("")
        assertEquals(2, viewModel.notes.value.size)
    }

    @Test
    fun `delete borra la nota y la emite para poder deshacer`() = runTest {
        val id = repository.save(Note(title = "Borrar", text = "x"))
        val note = repository.getById(id)!!

        viewModel.delete(note)

        assertNull(repository.getById(id))
        assertEquals(note, viewModel.deletedNotes.first())
    }

    @Test
    fun `deleteById borra la nota indicada`() = runTest {
        val id = repository.save(Note(title = "Borrar", text = "x"))

        viewModel.deleteById(id)

        assertNull(repository.getById(id))
        assertEquals(id, viewModel.deletedNotes.first().id)
    }

    @Test
    fun `undoDelete restaura la nota borrada`() = runTest {
        val id = repository.save(Note(title = "Recuperar", text = "x"))
        val note = repository.getById(id)!!
        viewModel.delete(note)

        viewModel.undoDelete(viewModel.deletedNotes.first())

        assertEquals(note, repository.getById(id))
        assertTrue(repository.notes.first().isNotEmpty())
    }
}
