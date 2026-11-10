package com.carlosalbertoxw.crud_android_sqlite.ui

import androidx.lifecycle.SavedStateHandle
import com.carlosalbertoxw.crud_android_sqlite.data.FakeNoteDao
import com.carlosalbertoxw.crud_android_sqlite.data.MAX_TEXT_LENGTH
import com.carlosalbertoxw.crud_android_sqlite.data.MAX_TITLE_LENGTH
import com.carlosalbertoxw.crud_android_sqlite.data.Note
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Pruebas JVM de [NoteFormViewModel]: carga, validación, guardado y borrador. */
class NoteFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeNoteDao
    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        dao = FakeNoteDao()
        repository = NoteRepository(dao)
    }

    private fun viewModel(noteId: Long, handle: SavedStateHandle = SavedStateHandle()) =
        NoteFormViewModel(noteId, handle, repository)

    @Test
    fun `al editar carga el titulo y el texto de la nota`() = runTest {
        val id = repository.save(Note(title = "Hola", text = "Mundo"))

        val vm = viewModel(id)

        assertTrue(vm.isEditing)
        assertEquals("Hola", vm.title.value)
        assertEquals("Mundo", vm.text.value)
        assertFalse("recién cargada no tiene cambios", vm.hasChanges.value)
    }

    @Test
    fun `el borrador sobrevive a recrear el ViewModel y no se sobrescribe`() = runTest {
        val id = repository.save(Note(title = "Original", text = "texto"))
        val handle = SavedStateHandle()
        viewModel(id, handle).onTitleChange("Editado sin guardar")

        // Simula una rotación: nuevo ViewModel con el mismo SavedStateHandle.
        val recreated = viewModel(id, handle)

        assertEquals("Editado sin guardar", recreated.title.value)
        assertTrue(recreated.hasChanges.value)
    }

    @Test
    fun `guardar con campos vacios emite error de validacion`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)
        vm.onTitleChange("   ")

        vm.save()

        assertEquals(FormEvent.ValidationError, vm.events.first())
        assertTrue(repository.notes.first().isEmpty())
    }

    @Test
    fun `guardar una nota nueva la inserta recortando espacios`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)
        vm.onTitleChange("  Título ")
        vm.onTextChange(" Contenido  ")

        vm.save()

        assertEquals(FormEvent.Saved, vm.events.first())
        val saved = repository.notes.first().single()
        assertEquals("Título", saved.title)
        assertEquals("Contenido", saved.preview)
    }

    @Test
    fun `pulsar guardar varias veces solo inserta una nota`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)
        vm.onTitleChange("Título")
        vm.onTextChange("Contenido")

        vm.save()
        vm.save()
        vm.save()

        assertEquals(1, repository.notes.first().size)
    }

    @Test
    fun `si falla el guardado avisa y permite reintentar`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)
        vm.onTitleChange("Título")
        vm.onTextChange("Contenido")
        dao.failWrites = true

        vm.save()
        assertEquals(FormEvent.SaveFailed, vm.events.first())

        dao.failWrites = false
        vm.save()
        assertEquals(FormEvent.Saved, vm.events.first())
        assertEquals(1, repository.notes.first().size)
    }

    @Test
    fun `los textos se recortan al maximo permitido`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)

        vm.onTitleChange("t".repeat(MAX_TITLE_LENGTH + 50))
        vm.onTextChange("x".repeat(MAX_TEXT_LENGTH + 50))

        assertEquals(MAX_TITLE_LENGTH, vm.title.value.length)
        assertEquals(MAX_TEXT_LENGTH, vm.text.value.length)
    }

    @Test
    fun `editar una nota que ya no existe emite NotFound`() = runTest {
        val vm = viewModel(noteId = 42L)

        assertEquals(FormEvent.NotFound, vm.events.first())
    }

    @Test
    fun `hasChanges detecta cambios en una nota nueva`() = runTest {
        val vm = viewModel(NEW_NOTE_ID)
        assertFalse(vm.hasChanges.value)

        vm.onTextChange("algo")

        assertTrue(vm.hasChanges.value)
    }
}
